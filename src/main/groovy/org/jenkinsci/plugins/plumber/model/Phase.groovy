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

import com.google.common.collect.ImmutableList
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings

import static org.jenkinsci.plugins.plumber.Utils.getTabs
import static org.jenkinsci.plugins.plumber.Utils.toArgForm

@ToString
@EqualsAndHashCode
@SuppressFBWarnings(value="SE_NO_SERIALVERSIONID")
public class Phase extends AbstractPlumberModel {
    String name
    List<String> before = []
    List<String> after = []
    Action action
    String label
    String dockerImage
    List<String> archiveDirs = []
    List<String> stashDirs = []
    List<Unstash> unstash = []
    Notifications notifications
    Map<String,String> env = [:]
    Boolean treatUnstableAsSuccess

    Phase name(String val) {
        fieldVal("name", val)
    }

    Phase label(String val) {
        fieldVal("label", val)
    }

    Phase dockerImage(String val) {
        fieldVal("dockerImage", val)
    }

    Phase before(String val) {
        addValToList("before", val)
    }

    Phase before(List<String> val) {
        fieldVal("before", val)
    }

    Phase before(String... val) {
        fieldVal("before", val)
    }

    Phase after(String val) {
        addValToList("after", val)
    }

    Phase after(List<String> val) {
        fieldVal("after", val)
    }

    Phase after(String... val) {
        fieldVal("after", val)
    }

    Phase archiveDir(String val) {
        addValToList("archiveDirs", val)
    }

    Phase archiveDirs(List<String> val) {
        fieldVal("archiveDirs", val)
    }

    Phase archiveDirs(String... val) {
        fieldVal("archiveDirs", val)
    }

    Phase stashDir(String val) {
        addValToList("stashDirs", val)
    }

    Phase stashDirs(List<String> val) {
        fieldVal("stashDirs", val)
    }

    Phase stashDirs(String... val) {
        fieldVal("stashDirs", val)
    }

    Phase action(Closure<?> closure) {
        closureVal("action", Action.class, closure)
    }

    Phase notifications(Closure<?> closure) {
        closureVal("notifications", Notifications.class, closure)
    }

    Phase unstash(Closure<?> closure) {
        addClosureValToList("unstash", Unstash.class, closure)
    }

    Phase unstash(String from, String dir) {
        addValToList("unstash", new Unstash().fromPhase(from).dir(dir))
    }

    Phase env(Map<String,String> val) {
        fieldVal("env", val)
    }

    Phase treatUnstableAsSuccess(Boolean val) {
        fieldVal("treatUnstableAsSuccess", val)
    }


    // Actually, I need to pass the root level in here too so that I know defaults for notifications etc.
    public List<String> toPipelineScript(Root root, int tabsDepth) {
        def tabs = getTabs(tabsDepth)
        def overrides = getOverrides(root)
        def overridesFlagsBase = overrides.findAll { it.value instanceof Boolean }
        def notifierFlagsBase = overrides.notifications.flags()
        def overridesFlagsString = toArgForm(overridesFlagsBase)

        def lines = []

        lines.addAll(nodeLabelOrDocker(overridesFlagsString, notifierFlagsBase, overrides, 0))

        return lines.collect { "${tabs}${it}" }
    }

    private List<String> phaseExecutionCode(String overridesFlagsString, Map<String,Boolean> notifierFlagsBase,
                                            Map<String,Object> overrides, int tabsDepth) {
        def lines = []
        def tabs = getTabs(tabsDepth)

        lines << "generalNotifier(${toArgForm(name)}, [${overridesFlagsString}], [${toArgForm([before: true] + notifierFlagsBase)}])"
        if (action.inputText == null) {
            lines << "checkout scm"

            if (!unstash.isEmpty()) {
                lines.addAll(unstash.collect { it.toPipelineScript(0) })
            }
        }

        lines << "catchError {"
        lines.addAll(action.toPipelineScript(this, 1))

        if (action.inputText == null) {
            // Archiving and stashing.
            if (!overrides.archiveDirs.isEmpty()) {
                lines << "archive ${toArgForm(overrides.archiveDirs)}"
            }

            if (!overrides.stashDirs.isEmpty()) {
                lines << "stash ${toArgForm(name)}, [${toArgForm(overrides.stashDirs)}]"
            }
        }
        lines << "}"

        lines << "generalNotifier(${toArgForm(name)}, [${overridesFlagsString}], [${toArgForm([before: false] + notifierFlagsBase)}])"

        return lines.collect { "${tabs}${it}" }
    }

    private List<String> envWrapper(String overridesFlagsString, Map<String,Boolean> notifierFlagsBase,
                                    Map<String,Object> overrides, int tabsDepth) {
        def tabs = getTabs(tabsDepth)
        def lines = []

        if (overrides.containsKey("envList") && overrides.envList != null && !overrides.envList.isEmpty()) {
            lines << "withEnv([${toArgForm(overrides.envList)}]) {"
            lines.addAll(phaseExecutionCode(overridesFlagsString, notifierFlagsBase, overrides, 1))
            lines << "}"
        } else {
            lines.addAll(phaseExecutionCode(overridesFlagsString, notifierFlagsBase, overrides, 0))
        }

        return lines.collect { "${tabs}${it}" }
    }

    private List<String> nodeLabelOrDocker(String overridesFlagsString, Map<String,Boolean> notifierFlagsBase,
                                           Map<String,Object> overrides, int tabsDepth) {
        def lines = []
        def tabs = getTabs(tabsDepth)

        if (action?.inputText != null) {
            // If we're prompting for input, don't wrap in a node.
            lines.addAll(envWrapper(overridesFlagsString, notifierFlagsBase, overrides, 0))
        } else if (label != null) {
            lines << "node('${label}') {"
            lines.addAll(envWrapper(overridesFlagsString, notifierFlagsBase, overrides, 1))
            lines << "}"
        } else if (dockerImage != null) {
            lines << "node('docker') { // TODO: Figure out how we specify the Docker node label"
            lines << "\tdocker.image('${dockerImage}').inside() {"
            lines.addAll(envWrapper(overridesFlagsString, notifierFlagsBase, overrides, 2))
            lines << "\t}"
            lines << "}"
        } else {
            lines << "node {"
            lines.addAll(envWrapper(overridesFlagsString, notifierFlagsBase, overrides, 1))
            lines << "}"
        }

        return lines.collect { "${tabs}${it}" }
    }

    /**
     * Get the archiveDirs, stashDirs, env and notifications for this phase, defaulting to the root versions if
     * not specified here.
     *
     * @param root The Root this phase is in.
     * @return A map of archiveDirs, stashDirs, env and notifications
     */
    public Map getOverrides(Root root) {
        def overrideMap = [:]

        if (this.archiveDirs.isEmpty()) {
            overrideMap.archiveDirs = ImmutableList.copyOf(root.archiveDirs)
        } else {
            overrideMap.archiveDirs = ImmutableList.copyOf(this.archiveDirs)
        }

        if (this.stashDirs.isEmpty()) {
            overrideMap.stashDirs = ImmutableList.copyOf(root.stashDirs)
        } else {
            overrideMap.stashDirs = ImmutableList.copyOf(this.stashDirs)
        }

        overrideMap.env = [:]
        overrideMap.env.putAll(root.env)

        overrideMap.env.putAll(this.env)

        if (this.notifications == null) {
            if (root.notifications != null) {
                overrideMap.notifications = root.notifications
            } else {
                overrideMap.notifications = new Notifications()
            }
        } else {
            overrideMap.notifications = this.notifications
        }

        if (this.treatUnstableAsSuccess == null) {
            overrideMap.treatUnstableAsSuccess = root.treatUnstableAsSuccess
        } else {
            overrideMap.treatUnstableAsSuccess = this.treatUnstableAsSuccess
        }

        // Shortcut to avoid having to do collect in Pipeline script.
        overrideMap.envList = overrideMap.env.collect { k, v -> "${k}=${v}" }

        overrideMap

    }

    static final int serialVersionUID = 1L

}
