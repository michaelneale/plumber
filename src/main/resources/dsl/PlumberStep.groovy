/*
 * The MIT License
 *
 * Copyright (c) 2016, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package dsl

import com.cloudbees.groovy.cps.NonCPS
import hudson.model.Result
import org.jenkinsci.plugins.plumber.model.Notifications
import org.jenkinsci.plugins.plumber.model.Phase
import org.jenkinsci.plugins.plumber.model.PlumberConfig
import org.jenkinsci.plugins.plumber.model.Root
import org.jenkinsci.plugins.plumber.model.Unstash
import org.jenkinsci.plugins.workflow.cps.CpsScript

class PlumberInterpreter implements Serializable {
    private CpsScript script;

    public PlumberInterpreter(CpsScript script) {
        this.script = script;
    }

    def call(Closure c) {
        PlumberConfig config = new PlumberConfig()
        config.fromClosure(c)

        Root root = config.getConfig()

        def executionSets = root.executionSets()

        for (int i = 0; i < executionSets.size(); i++) {
            def exSet = executionSets.get(i)

            debugLog(root.debug, "Creating stage ${exSet.stageName}")
            script.stage exSet.stageName
            parallelizePhases(root, exSet.phases)
        }
    }

    def constructPhase(Root root, Phase phase) {
        def overrides = phase.getOverrides(root)

        return {
            debugLog(root.debug, "Determining whether to run in node/label/docker")
            nodeLabelOrDocker(phase, root.debug) {

                debugLog(root.debug, "Determining environment overrides")
                envWrapper(overrides, root.debug) {

                    // Pre-phase notifier.
                    debugLog(root.debug, "Pre-phase notifier")
                    generalNotifier(true, root.debug, overrides, phase)

                    debugLog(root.debug, "Checkout SCM")
                    // TODO: Move to conditionals around SCM checkout
                    script.checkout(script.scm)

                    if (!phase.unstash.isEmpty()) {
                        debugLog(root.debug, "Unstash configs found")
                        for (int i = 0; i < phase.unstash.size(); i++) {
                            Unstash s = phase.unstash.get(i)
                            debugLog(root.debug, "Unstashing from phase ${s.fromPhase}")
                            script.unstash(s.fromPhase)
                        }
                    }

                    debugLog(root.debug, "Executing action, wrapped in catchError")
                    // Phase execution
                    script.catchError {
                        if (phase.action.plunger != null && !phase.action.plunger.isEmpty()) {
                            // TODO: Write the actual step!
                            debugLog(root.debug, "Running plunger ${phase.action.plunger.name}")
                            script.runPlunger(phase.action.plunger.delegate.clone())
                        } else if (phase.action.script != null) {
                            debugLog(root.debug, "Running script '${phase.action.script}'")
                            if (script.isUnix()) {
                                debugLog(root.debug, "...on Unix, so sh.")
                                script.sh(phase.action.script)
                            } else {
                                debugLog(root.debug, "...not on Unix, so bat.")
                                script.bat(phase.action.script)
                            }
                        } else if (phase.action.inputText != null) {
                            debugLog(root.debug, "Prompting for input.")
                            // Input step execution - TODO - read up on that
                        } else {
                            debugLog(root.debug, "ERROR: No plunger, script or inputText specified")
                            script.error("No plunger, script or inputText specified")
                        }
                    }

                    // Archiving and stashing.
                    if (!overrides.archiveDirs.isEmpty()) {
                        debugLog(root.debug, "Archiving directories/files ${overrides.archiveDirs}")
                        script.archive(overrides.archiveDirs)
                    }

                    if (!overrides.stashDirs.isEmpty()) {
                        debugLog(root.debug, "Stashing directories/files ${overrides.stashDirs}")
                        script.stash(phase.name, overrides.stashDirs)
                    }

                    // Post-phase notifier
                    debugLog(root.debug, "Post-phase notifier")
                    generalNotifier(false, root.debug, overrides, phase)
                }
            }
        }
    }


    private Closure generalNotifier(Boolean before, Boolean debug, Map overrides, Phase phase) {
        Notifications n = overrides.notifications

        def notifySteps = []

        def shouldSend = false

        if (before) {
            // We'll send pre-phase emails whenever "beforePhase" is set or if this an inputText phase.
            if (n.beforePhase || phase.action.inputText != null) {
                shouldSend = true
            }
        } else {
            Result failureResult
            if (overrides.treatUnstableAsSuccess) {
                failureResult = Result.FAILURE
            } else {
                failureResult = Result.UNSTABLE
            }

            if (((Result)script.currentBuild.rawBuild.getResult()).isBetterThan(failureResult)) {
                if (n.onSuccess) {
                    shouldSend = true
                }
            } else {
                if (n.onFailure) {
                    shouldSend = true
                }
            }
        }

        return {
            debugLog(debug, "Checking if should send notifications...")
            if (shouldSend && n.allPhases && !n.skipThisPhase) {
                debugLog(debug, "And should send notifications!")
                for (int i = 0; i < n.configs.entrySet().size(); i++) {
                    def entry = n.configs.entrySet().toList().get(i)
                    def config = entry.value?.delegate

                    if (config != null) {
                        config.type = entry.key
                        config.phaseName = phase.name

                        config.before = before

                        debugLog(debug, "Notifying to ${config.type}")

                        // TODO: Actually write the script!
                        script.runPlumberNotifier(config)
                    }
                }
            }
        }
    }

    /**
     * Wraps the given body in a "withEnv" block set to use the properly overridden environment variables.
     *
     * @param overrides
     * @param debug
     * @param body
     *
     * @return a Closure
     */
    private Closure envWrapper(Map overrides, Boolean debug, Closure body) {
        if (overrides.containsKey("envList") && overrides.envList != null && !overrides.envList.isEmpty()) {
            return {
                debugLog(debug, "Overriding env with ${overrides.envList}")
                script.withEnv(overrides.envList) {
                    body
                }
            }
        } else {
            return {
                debugLog(debug, "No env overrides, proceeding.")
                body
            }
        }
    }

    /**
     * Wraps the given body in a node block, possibly with a docker.image.inside block within it as appropriate.
     *
     * @param phase
     * @param debug
     * @param body
     *
     * @return a Closure. That does things. But not too soon. Hopefully.
     */
    private Closure nodeLabelOrDocker(Phase phase, Boolean debug, Closure body) {
        if (phase.label != null) {
            return {
                debugLog(debug, "Running in label ${phase.label}")
                script.node(phase.label) {
                    body
                }
            }
        } else if (phase.dockerImage != null) {
            return {
                debugLog(debug, "Running in docker image ${phase.dockerImage}")
                script.node("docker") { // TODO: Figure out how we specify the Docker node label
                    script.docker.image(phase.dockerImage).inside() {
                        body
                    }
                }
            }
        } else {
            return {
                debugLog(debug, "Running on arbitrary node")
                script.node {
                    body
                }
            }
        }
    }

    private def debugLog(Boolean debug, String msg) {
        if (debug) {
            return script.echo("PLUMBER_DEBUG: ${msg}")
        } else {
            return null
        }
    }

    @NonCPS
    private def parallelizePhases(Root root, List<Phase> phases) {
        return {
            debugLog(root.debug, "Checking for how to run phases...")
            if (phases.size() > 1) {
                debugLog(root.debug, "Multiple phases in an execution set, run in parallel")
                script.parallel(phases.collectEntries { p ->
                    [(p.name): constructPhase(root, p)]
                })
            } else if (!phases.isEmpty()) {
                debugLog(root.debug, "Single phase in an execution set, run alone")
                constructPhase(root, phases[0])
            } else {
                debugLog(root.debug, "No phases in execution set - skipping?")
            }
        }
    }
}