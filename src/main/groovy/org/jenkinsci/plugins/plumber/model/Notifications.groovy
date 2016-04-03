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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings

@ToString
@EqualsAndHashCode
@SuppressFBWarnings(value="SE_NO_SERIALVERSIONID")
public class Notifications extends AbstractPlumberModel {

    Map<String,MappedClosure> configs = [:]
    Boolean allPhases = true
    Boolean skipThisPhase = false
    Boolean onSuccess = false
    Boolean onFailure = true
    Boolean beforePhase = false

    public Notifications() {

    }

    public Notifications(Map<String,Object> args) {
        if (args != null) {
            if (args.containsKey("allPhases")) {
                this.allPhases = args.allPhases
            }
            if (args.containsKey("skipThisPhase")) {
                this.skipThisPhase = args.skipThisPhase
            }
            if (args.containsKey("onSuccess")) {
                this.onSuccess = args.onSuccess
            }
            if (args.containsKey("onFailure")) {
                this.onFailure = args.onFailure
            }
            if (args.containsKey("beforePhase")) {
                this.beforePhase = args.beforePhase
            }

            args.configs?.each { String name, Map<String,Object> conf ->
                this.configs."${name}" = new MappedClosure(conf)
            }
        }
    }

    Notifications config(String type, Closure<?> closure) {
        addClosureValToMap("configs", MappedClosure.class, type, closure)
    }

    Notifications allPhases(Boolean val) {
        fieldVal("allPhases", val)
    }

    Notifications skipThisPhase(Boolean val) {
        fieldVal("skipThisPhase", val)
    }

    Notifications onSuccess(Boolean val) {
        fieldVal("onSuccess", val)
    }

    Notifications onFailure(Boolean val) {
        fieldVal("onFailure", val)
    }

    Notifications beforePhase(Boolean val) {
        fieldVal("beforePhase", val)
    }

    static final int serialVersionUID = 1L

    public static String toPipelineScriptFunction() {
        def lines = []

        lines << "def generalNotifier(String phaseName, Map<String,Boolean> flags, Map<String,Map> notifiers) {"
        lines << "\tdef shouldSend = false"

        lines << "\tif (flags.before) {"
        lines << "\t\tif (flags.beforePhase || flags.hasInput != null) {"
        lines << "\t\t\tshouldSend = true"
        lines << "\t\t}"
        lines << "\t} else {"
        lines << "\t\tResult failureResult"
        lines << "\t\tif (flags.treatUnstableAsSuccess) {"
        lines << "\t\t\tfailureResult = Result.FAILURE"
        lines << "\t\t} else {"
        lines << "\t\t\tfailureResult = Result.UNSTABLE"
        lines << "\t\t}"

        lines << "\t\tdef currentResult = currentBuild.rawBuild.getResult()"
        lines << "\t\tif (currentResult == null || currentResult.isBetterThan(failureResult)) {"
        lines << "\t\t\tif (flags.onSuccess) {"
        lines << "\t\t\t\tshouldSend = true"
        lines << "\t\t\t}"
        lines << "\t\t} else {"
        lines << "\t\t\tif (flags.onFailure) {"
        lines << "\t\t\t\tshouldSend = true"
        lines << "\t\t\t}"
        lines << "\t\t}"
        lines << "\t}"
        lines << "\n"
        lines << "\tif (shouldSend && flags.allPhases && !flags.skipThisPhase) {"
        lines << "\t\tdef theseNotifiers = getNotifiers(phase.name, before, n.configs)"
        lines << "\t\tfor (int i = 0; i < theseNotifiers.size(); i++) {"
        lines << "\t\t\tdef thisNotifier = theseNotifiers.get(i)"
        lines << "\t\t\tscript.getProperty('runPipelineAction').call(PipelineActionType.NOTIFIER, thisNotifier)"
        lines << "\t\t}"
        lines << "\t}"
        lines << "}"
        lines << "@NonCPS"
        lines << "private List<Map<String,Object>> getNotifiers(String phaseName, Boolean before, Map<String,MappedClosure> configs) {"
        lines << "\tdef notifiers = []"
        lines << "\tconfigs.each { k, v ->"
        lines << "\t\tdef conf = v?.getMap()"
        lines << "\t\tif (conf != null) {"
        lines << "\t\t\tconf.name = k"
        lines << "\t\t\tconf.phaseName = phaseName"
        lines << "\t\t\tconf.before = before"
        lines << "\t\t\tnotifiers << conf"
        lines << "\t\t}"
        lines << "\t}"
        lines << "\treturn notifiers"
        lines << "}"

        return lines.join("\n")

    }

}
