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

    @NonCPS
    def constructPhase(Root root, Phase phase) {
        return nodeLabelOrDocker(phase) {

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
    @NonCPS
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