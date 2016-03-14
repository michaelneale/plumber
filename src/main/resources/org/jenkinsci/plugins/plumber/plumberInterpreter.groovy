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

import hudson.model.Result
import org.jenkinsci.plugins.plumber.model.Notifications
import org.jenkinsci.plugins.plumber.model.Phase
import org.jenkinsci.plugins.plumber.model.Root
import org.jenkinsci.plugins.workflow.cps.CpsScript

class PlumberInterpreter implements Serializable {
    private CpsScript script;

    public PlumberInterpreter(CpsScript script) {
        this.script = script;
    }

    def call(Root root) {
    }

    def constructPhase(Root root, Phase phase) {
        def overrides = phase.getOverrides(root)

        return nodeLabelOrDocker(phase) {
            envWrapper(overrides) {
                // Pre-phase notifier.
                generalNotifier(true, overrides, phase)

                // Phase execution
                script.catchError {
                    if (phase.action.plunger != null && !phase.action.plunger.isEmpty()) {
                        // TODO: Write the actual step!
                        script.runPlunger(phase.action.plunger.delegate.clone())
                    } else if (phase.action.script != null) {
                        if (script.isUnix()) {
                            script.sh(phase.action.script)
                        } else {
                            script.bat(phase.action.script)
                        }
                    } else if (phase.action.inputText != null) {
                        // Input step execution - TODO - read up on that
                    } else {
                        script.error("No plunger, script or inputText specified")
                    }
                }

                // Post-phase notifier
                generalNotifier(false, overrides, phase)
            }
        }
    }


    private Closure generalNotifier(Boolean before, Map overrides, Phase phase) {
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
            if (shouldSend && n.allPhases && !n.skipThisPhase) {
                for (int i = 0; i < n.configs.entrySet().size(); i++) {
                    def entry = n.configs.entrySet().toList().get(i)
                    def config = entry.value?.delegate

                    if (config != null) {
                        config.type = entry.key
                        config.phaseName = phase.name

                        config.before = before

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
     * @param body
     *
     * @return a Closure
     */
    private Closure envWrapper(Map overrides, Closure body) {
        if (overrides.containsKey("envList") && overrides.envList != null && !overrides.envList.isEmpty()) {
            return script.withEnv(overrides.envList) {
                body
            }
        } else {
            return body
        }
    }

    /**
     * Wraps the given body in a node block, possibly with a docker.image.inside block within it as appropriate.
     *
     * @param phase
     * @param body
     *
     * @return a Closure. That does things. But not too soon. Hopefully.
     */
    private Closure nodeLabelOrDocker(Phase phase, Closure body) {
        if (phase.label != null) {
            return script.node(phase.label) {
                body
            }
        } else if (phase.dockerImage != null) {
            return script.node("docker") { // TODO: Figure out how we specify the Docker node label
                script.docker.image(phase.dockerImage).inside() {
                    body
                }
            }
        } else {
            return script.node {
                body
            }
        }
    }
}