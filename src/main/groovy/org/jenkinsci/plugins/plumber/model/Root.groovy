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
package org.jenkinsci.plugins.plumber.model

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.jenkinsci.plugins.plumber.PlumberDependencyGraph

@ToString
@EqualsAndHashCode
public class Root extends AbstractPlumberModel {

    List<Phase> phases = []
    Map<String,String> env = [:]
    Notifications notifications
    List<String> archiveDirs = []
    List<String> stashDirs = []
    Boolean treatUnstableAsSuccess = false
    Boolean debug = false


    Root phase(Closure<?> closure) {
        addClosureValToList("phases", Phase.getClass(), closure)
    }

    Root env(Map<String,String> val) {
        fieldVal("env", val)
    }

    Root notifications(Closure<?> closure) {
        closureVal("notifications", Notifications.getClass(), closure)
    }

    Root archiveDir(String val) {
        addValToList("archiveDirs", val)
    }

    Root archiveDirs(List<String> val) {
        fieldVal("archiveDirs", val)
    }

    Root archiveDirs(String... val) {
        fieldVal("archiveDirs", val)
    }

    Root stashDir(String val) {
        addValToList("stashDirs", val)
    }

    Root stashDirs(List<String> val) {
        fieldVal("stashDirs", val)
    }

    Root stashDirs(String... val) {
        fieldVal("stashDirs", val)
    }

    Root treatUnstableAsSuccess(Boolean val) {
        fieldVal("treatUnstableAsSuccess", val)
    }

    Root debug(Boolean val) {
        fieldVal("debug", val)
    }

    /**
     * Finds a phase with the given name and returns it.
     *
     * @param name
     * @return the Phase matching the name, or null if not found.
     */
    public Phase phaseFromName(String name) {
        if (name != null && name != "") {
            return phases.find { it.name == name }
        } else {
            return null
        }
    }

    /**
     * Returns a list of "execution sets" - groups of 1..n phases to be executed concurrently, with a stage name
     * constructed from the phase names as well.
     *
     * @return A list of maps with "stageName" and "phases" keys.
     */
    public List executionSets() {
        def graph = PlumberDependencyGraph.fromPhaseList(phases)

        def exSets = []

        while (graph.hasMorePhases()) {
            def exSetDetails = [:]
            def exSetPhaseNames = graph.getNextPhases()

            exSetDetails.stageName = exSetPhaseNames.join("+")

            exSetDetails.phases = exSetPhaseNames.collect { phaseFromName(it) }

            exSets << exSetDetails

            graph.postPhaseProcessing(exSetPhaseNames)
        }

        return exSets
    }

    static final int serialVersionUID = 1L

}
