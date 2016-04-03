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
import io.jenkins.plugins.pipelineaction.PipelineAction

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
    Matrix matrix
    List<SCM> scms = []
    Boolean skipSCM

    public Phase() {

    }

    public Phase(Map<String, Object> args) {
        if (args != null) {
            this.name = args.name
            if (args.containsKey("before")) {
                if (args.before instanceof String) {
                    this.before = [(String)args.before]
                } else if (args.before instanceof List) {
                    this.before = (List<String>)args.before
                }
            }
            if (args.containsKey("after")) {
                if (args.after instanceof String) {
                    this.after = [(String)args.after]
                } else if (args.after instanceof List) {
                    this.after = (List<String>)args.after
                }
            }
            if (args.containsKey("action") && args.action instanceof Map) {
                this.action = new Action((Map<String,Object>)args.action)
            }
            this.label = args.label
            this.dockerImage = args.dockerImage
            if (args.containsKey("archiveDirs")) {
                if (args.archiveDirs instanceof String) {
                    this.archiveDirs = [(String)args.archiveDirs]
                } else if (args.archiveDirs instanceof List) {
                    this.archiveDirs = (List<String>)args.archiveDirs
                }
            }
            if (args.containsKey("stashDirs")) {
                if (args.stashDirs instanceof String) {
                    this.stashDirs = [(String)args.stashDirs]
                } else if (args.stashDirs instanceof List) {
                    this.stashDirs = (List<String>)args.stashDirs
                }
            }
            if (args.containsKey("scm") && args.scm instanceof List) {
                args.scm?.each { Map<String,Object> scmMap ->
                    this.scms.add(new SCM(scmMap))
                }
            }
            if (args.containsKey("unstash") && args.unstash instanceof List) {
                args.unstash?.each { Map<String,Object> unstashMap ->
                    this.unstash.add(new Unstash(unstashMap))
                }
            }
            if (args.containsKey("notifications") && args.notifications instanceof Map) {
                this.notifications = new Notifications((Map<String,Object>) args.notifications)
            }
            if (args.containsKey("env") && args.env instanceof Map) {
                args.env?.each { String k, String v ->
                    this.env.put(k, v)
                }
            }
            if (args.containsKey("matrix") && args.matrix instanceof Map) {
                this.matrix = new Matrix((Map<String,Object>)args.matrix)
            }
            if (args.containsKey("treatUnstableAsSuccess")) {
                this.treatUnstableAsSuccess = args.treatUnstableAsSuccess
            }
            if (args.containsKey("skipSCM")) {
                this.skipSCM = args.skipSCM
            }
        }
    }

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

    Phase matrix(Closure<?> closure) {
        closureVal("matrix", Matrix.class, closure)
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

    Phase scm(Closure<?> closure) {
        addClosureValToList("scms", SCM.class, closure)
    }

    Phase env(Map<String,String> val) {
        fieldVal("env", val)
    }

    Phase treatUnstableAsSuccess(Boolean val) {
        fieldVal("treatUnstableAsSuccess", val)
    }

    Phase skipSCM(Boolean val) {
        fieldVal("skipSCM", val)
    }

    public void addToEnv(String key, Object value) {
        this.env."${key}" = value
    }

    public void addToEnv(Map<String, Object> inputMap) {
        this.env.putAll(inputMap)
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

        def actionConfig = action?.actionConfig?.getMap()

        PipelineAction actionClass
        if (actionConfig != null && !actionConfig.isEmpty() && actionConfig.name != null) {
            actionClass = PipelineAction.getPipelineAction(actionConfig.name)
        }

        lines << "generalNotifier(${toArgForm(name)}, [${overridesFlagsString}], [${toArgForm([before: true] + notifierFlagsBase)}])"
        if (actionClass != null && actionClass.usesNode()) {
            if (!overrides.skipSCM) {
                if (overrides.containsKey("scms") && overrides.scms != null && !overrides.scms.isEmpty()) {
                    overrides.scms.each { SCM s ->
                        lines.addAll(s.toPipelineScript(this, 0))
                    }
                } else {
                    lines << "checkout scm"
                }
            }

            if (!unstash.isEmpty()) {
                lines.addAll(unstash.collect { it.toPipelineScript(0) })
            }
        }

        lines << "catchError {"
        lines.addAll(action.toPipelineScript(this, 1))

        if (actionClass != null && actionClass.usesNode()) {
            // Archiving and stashing.
            if (!overrides.archiveDirs.isEmpty()) {
                lines << "archive ${toArgForm(overrides.archiveDirs)}"
            }

            if (!overrides.stashDirs.isEmpty()) {
                lines << "stash name: ${toArgForm(name)}, includes: ${toArgForm(overrides.stashDirs)}"
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

        def actionConfig = action?.actionConfig?.getMap()
        PipelineAction actionClass
        if (actionConfig != null && !actionConfig.isEmpty() && actionConfig.name != null) {
            actionClass = PipelineAction.getPipelineAction(actionConfig.name)
        }
        if (actionClass != null && !actionClass.usesNode()) {
            // If we're running an action that doesn't have usesNode()==true, don't wrap in a node.
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
     * Get the archiveDirs, stashDirs, env, scm and notifications for this phase, defaulting to the root versions if
     * not specified here.
     *
     * @param root The Root this phase is in.
     * @return A map of archiveDirs, stashDirs, env, scm and notifications
     */
    public Map getOverrides(Root root) {
        def overrideMap = [:]

        if (this.archiveDirs.isEmpty()) {
            overrideMap.archiveDirs = root.archiveDirs?.join(',')
        } else {
            overrideMap.archiveDirs = this.archiveDirs?.join(',')
        }

        if (this.stashDirs.isEmpty()) {
            overrideMap.stashDirs = root.stashDirs?.join(',')
        } else {
            overrideMap.stashDirs = this.stashDirs?.join(',')
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

        if (this.skipSCM == null) {
            overrideMap.skipSCM = root.skipSCM
        } else {
            overrideMap.skipSCM = this.skipSCM
        }

        if (this.scms.isEmpty()) {
            overrideMap.scms = ImmutableList.copyOf(root.scms)
        } else {
            overrideMap.scms = ImmutableList.copyOf(this.scms)
        }

        // Shortcut to avoid having to do collect in Pipeline script.
        overrideMap.envList = overrideMap.env.collect { k, v -> "${k}=${v}" }

        overrideMap

    }

    static final int serialVersionUID = 1L

}
