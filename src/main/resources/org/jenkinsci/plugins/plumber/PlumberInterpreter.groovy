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
package org.jenkinsci.plugins.plumber

import com.cloudbees.groovy.cps.NonCPS
import hudson.model.Result
import io.jenkins.plugins.pipelineaction.PipelineAction
import io.jenkins.plugins.pipelineaction.PipelineActionType
import org.jenkinsci.plugins.plumber.model.Action
import org.jenkinsci.plugins.plumber.model.MappedClosure
import org.jenkinsci.plugins.plumber.model.Notifications
import org.jenkinsci.plugins.plumber.model.Phase
import org.jenkinsci.plugins.plumber.model.PlumberConfig
import org.jenkinsci.plugins.plumber.model.Root
import org.jenkinsci.plugins.plumber.model.SCM
import org.jenkinsci.plugins.plumber.model.Unstash
import org.jenkinsci.plugins.workflow.cps.CpsScript

class PlumberInterpreter implements Serializable {
    private CpsScript script;

    public PlumberInterpreter(CpsScript script) {
        this.script = script;
    }

    def call(String closureString, Boolean doCodeGen = false) {
        Root root = getRootConfig(closureString)
        executePipeline(root, doCodeGen)
    }

    def call(Closure closure, Boolean doCodeGen = false) {
        Root root = getRootConfig(closure)
        executePipeline(root, doCodeGen)
    }

    def call(Map config, Boolean doCodeGen = false) {
        Root root = getRootConfig(config)
        executePipeline(root, doCodeGen)
    }

    private void executePipeline(Root root, Boolean doCodeGen) {

        if (doCodeGen) {
            String code = root.toPipelineScript().join("\n")

            def flow
            script.node {
                script.writeFile(file: "tmp.groovy", text: code)
                flow = script.load "tmp.groovy"
            }
            flow.call()
        } else {
            def executionSets = root.executionSets()

            for (int i = 0; i < executionSets.size(); i++) {
                def exSet = executionSets.get(i)

                debugLog(root.debug, "Creating stage ${exSet.stageName}")
                script.stage exSet.stageName
                parallelizePhases(root, exSet.phases).call()
            }
        }
    }


    @NonCPS
    def getRootConfig(Closure c) {
        def conf = new PlumberConfig()
        conf.fromClosure(c)
        return conf.getConfig()
    }

    @NonCPS
    def getRootConfig(String s) {
        def conf = new PlumberConfig()
        conf.fromString("{ -> ${s} }")
        return conf.getConfig()
    }

    @NonCPS
    def getRootConfig(Map m) {
        def conf = new PlumberConfig()
        conf.fromMap(m)
        return conf.getConfig()
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

                    if (!overrides.skipSCM) {
                        if (overrides.containsKey("scms") && overrides.scms != null && !overrides.scms.isEmpty()) {
                            debugLog(root.debug, "SCM overrides specified")
                            overrides.scms.each { SCM s ->
                                if (overrides.scms.size() > 1 && (s.directory == null || s.directory == "")) {
                                    script.error("More than one SCM specified, and SCM specified without a directory, so failing.")
                                } else {
                                    def argMap = [:]
                                    argMap.putAll(s.config.getMap())

                                    argMap.put("name", s.scmName)
                                    if (s.directory != null) {
                                        debugLog(root.debug, "Checking out with ${s.scmName} to directory ${s.directory}")
                                        script.dir(s.directory) {
                                            script.getProperty("runPipelineAction").call(PipelineActionType.SCM, argMap)
                                        }
                                    } else {
                                        debugLog(root.debug, "Checking out with ${s.scmName} to root directory")
                                        script.getProperty("runPipelineAction").call(PipelineActionType.SCM, argMap)
                                    }
                                }
                            }
                        } else {
                            debugLog(root.debug, "Default SCM behavior")
                            script.checkout(script.scm)
                        }
                    } else {
                        debugLog(root.debug, "SCM checkout skipped")
                    }

                    if (!phase.unstash.isEmpty()) {
                        debugLog(root.debug, "Unstash configs found")
                        for (int i = 0; i < phase.unstash.size(); i++) {
                            Unstash s = phase.unstash.get(i)
                            debugLog(root.debug, "Unstashing from phase ${s.fromPhase}")
                            def unstashDir = s.dir
                            if (unstashDir == null) {
                                unstashDir = script.pwd()
                            }
                            script.dir(unstashDir) {
                                script.unstash(s.fromPhase)
                            }
                        }
                    }

                    debugLog(root.debug, "Executing action, wrapped in catchError")
                    // Phase execution
                    script.catchError {
                        def actionMap = phase.action?.getMap()
                        if (actionMap != null && !actionMap.isEmpty()) {
                            // TODO: Write the actual step!
                            debugLog(root.debug, "Running action ${actionMap.name ?: 'script'}")
                            script.getProperty("runPipelineAction").call(PipelineActionType.STANDARD, actionMap)
                        } else {
                            debugLog(root.debug, "ERROR: No action specified")
                            script.error("No action specified")
                        }
                    }

                    // Archiving and stashing.
                    if (!overrides.archiveDirs.isEmpty()) {
                        try {
                            debugLog(root.debug, "Archiving directories/files ${overrides.archiveDirs}")
                            script.archive(overrides.archiveDirs)
                        } catch (Exception e) {
                            script.echo("Error archiving ${overrides.archiveDirs}, but continuing: ${e}")
                        }
                    }

                    if (!overrides.stashDirs.isEmpty()) {
                        debugLog(root.debug, "Stashing directories/files ${overrides.stashDirs}")
                        try {
                            script.stash(name: phase.name, includes: overrides.stashDirs)
                        } catch (Exception e) {
                            script.echo("Error stashing ${overrides.stashDirs}, but continuing: ${e}")
                        }
                    }

                    if (!phase.reporters.isEmpty()) {
                        debugLog(root.debug, "Running configured reporters")
                        phase.reporters.each { r ->
                            try {
                                debugLog(root.debug, "Running reporter ${r.reporterName}")
                                script.getProperty("runPipelineAction").call(PipelineActionType.REPORTER, r.config.getMap())
                            } catch (Exception e) {
                                script.echo("Error running reporter ${r.reporterName} with config ${r.config.getMap()}, but continuing: ${e}")
                            }
                        }
                    }

                    // Post-phase notifier
                    debugLog(root.debug, "Post-phase notifier")
                    generalNotifier(false, root.debug, overrides, phase)
                }.call()
            }.call()
        }
    }


    private Closure generalNotifier(Boolean before, Boolean debug, Map overrides, Phase phase) {
        Notifications n = overrides.notifications

        def notifySteps = []

        def shouldSend = false
        def actualAction = getActualAction(phase.action)

        if (before) {
            // We'll send pre-phase emails whenever "beforePhase" is set or if this an input phase.
            if (n.beforePhase || (actualAction != null && actualAction.name == "input")) {
                shouldSend = true
            }
        } else {
            Result failureResult
            if (overrides.treatUnstableAsSuccess) {
                failureResult = Result.FAILURE
            } else {
                failureResult = Result.UNSTABLE
            }

            def currentResult = script.$build().getResult()

            if (currentResult == null || currentResult.isBetterThan(failureResult)) {
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
                def notifiers = getNotifiers(phase.name, before, n.configs)
                for (int i = 0; i < notifiers.size(); i++) {
                    def thisNotifier = notifiers.get(i)
                    script.getProperty("runPipelineAction").call(PipelineActionType.NOTIFIER, thisNotifier)
                }
            }
        }.call()
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
                    body.call()
                }
            }
        } else {
            return {
                debugLog(debug, "No env overrides, proceeding.")
                body.call()
            }
        }
    }

    /**
     * Wraps the given body in a node block, possibly with a docker.image.inside block within it as appropriate. If the
     * phase's action is input, don't put anything in a node at all.
     *
     * @param phase
     * @param debug
     * @param body
     *
     * @return a Closure. That does things. But not too soon. Hopefully.
     */
    private Closure nodeLabelOrDocker(Phase phase, Boolean debug, Closure body) {
        def actualAction = getActualAction(phase.action)

        if (actualAction != null && !actualAction.usesNode()) {
            // If we're prompting for input, don't wrap in a node.
            return {
                debugLog(debug, "Running on flyweight executor for input")
                body.call()
            }
        } else if (phase.label != null) {
            return {
                debugLog(debug, "Running in label ${phase.label}")
                script.node(phase.label) {
                    if (phase.clean) {
                        debugLog(debug, "Cleaning workspace before phase execution")
                        script.deleteDir()
                    }
                    body.call()
                }
            }
        } else if (phase.dockerImage != null) {
            return {
                debugLog(debug, "Running in docker image ${phase.dockerImage}")
                script.node("docker") { // TODO: Figure out how we specify the Docker node label
                    script.docker.image(phase.dockerImage).inside() {
                        body.call()
                    }
                }
            }
        } else {
            return {
                debugLog(debug, "Running on arbitrary node")
                script.node {
                    if (phase.clean) {
                        debugLog(debug, "Cleaning workspace before phase execution")
                        script.deleteDir()
                    }
                    body.call()
                }
            }
        }
    }

    private def debugLog(Boolean debug, String msg) {
        if (debug) {
            System.err.println "PLUMBER_DEBUG: ${msg}"
            return script.echo("PLUMBER_DEBUG: ${msg}")
        }
    }

    private def parallelizePhases(Root root, List<Phase> phases) {
        return {
            debugLog(root.debug, "Checking for how to run phases...")
            if (phases.size() > 1) {
                debugLog(root.debug, "Multiple phases in an execution set, run in parallel")
                def parallelPhases = [:]
                for (int i = 0; i < phases.size(); i++) {
                    def phase = phases.get(i)
                    parallelPhases[phase.name] = constructPhase(root, phase)
                }
                script.parallel(parallelPhases)
            } else if (!phases.isEmpty()) {
                debugLog(root.debug, "Single phase in an execution set, run alone")
                constructPhase(root, phases[0]).call()
            } else {
                debugLog(root.debug, "No phases in execution set - skipping?")
            }
        }
    }

    private PipelineAction getActualAction(Action action, PipelineActionType type = PipelineActionType.STANDARD) {
        def actionConfig = action?.actionConfig?.getMap()
        PipelineAction actionClass
        if (actionConfig != null && !actionConfig.isEmpty() && actionConfig.name != null) {
            actionClass = PipelineAction.getPipelineAction(actionConfig.name, type)
        }
        return actionClass
    }

    @NonCPS
    private List<Map<String,Object>> getNotifiers(String phaseName, Boolean before, Map<String,MappedClosure> configs) {
        def notifiers = []
        configs.each { k, v ->
            def conf = v?.getMap()
            if (conf != null) {
                conf.name = k
                conf.phaseName = phaseName
                conf.before = before
                notifiers << conf
            }
        }

        return notifiers
    }
}
