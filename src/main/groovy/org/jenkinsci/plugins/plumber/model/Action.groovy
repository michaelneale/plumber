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

import static org.jenkinsci.plugins.plumber.Utils.getTabs
import static org.jenkinsci.plugins.plumber.Utils.toArgForm

@ToString
@EqualsAndHashCode
@SuppressFBWarnings(value="SE_NO_SERIALVERSIONID")
public class Action extends AbstractPlumberModel {
    MappedClosure funnel
    String script
    String inputText

    public Action() {

    }

    public Action(Map<String,Object> args) {
        if (args != null) {
            if (args.containsKey("funnel") && args.funnel instanceof Map) {
                this.funnel = new MappedClosure((Map<String,Object>)args.funnel)
            }
            this.script = args.script
            this.inputText = args.inputText
        }
    }

    Action funnel(Closure<?> closure) {
        closureVal("funnel", MappedClosure.class, closure)
    }

    Action funnel(String name, Closure<?> closure) {
        closureVal("funnel", MappedClosure.class, closure)
        this.funnel.name = name
        return this
    }

    Action funnel(String val) {
        closureVal("funnel", MappedClosure.class, { name val })
    }

    Action script(String script) {
        fieldVal("script", script)
    }

    Action inputText(String inputText) {
        fieldVal("inputText", inputText)
    }

    public List<String> toPipelineScript(Phase parent, int tabsDepth) {
        def tabs = getTabs(tabsDepth)

        def lines = []

        if (script != null) {
            lines << "if (isUnix()) {"
            lines << "\tsh \"${script}\""
            lines << "} else {"
            lines << "\tbat \"${script}\""
            lines << "}"
        } else if (inputText != null) {
            lines << "\tinput(message: \"${inputText}\", id: \"${parent.name}+input\")"
        } else if (!funnel.isEmpty()) {
            // If we don't have a name we're broken.
            if (funnel.containsKey("name")) {
                lines << "runFunnel([${toArgForm(funnel.getMap())}])"
            } else {
                // TODO: Error out! No name!
            }
        }

        return lines.collect { "${tabs}${it}" }
    }

    static final int serialVersionUID = 1L

}
