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

import static org.jenkinsci.plugins.plumber.Utils.getTabs
import static org.jenkinsci.plugins.plumber.Utils.toArgForm

@ToString
@EqualsAndHashCode
public class Action extends AbstractPlumberModel {
    MappedClosure plunger
    String script
    String inputText

    public Action() {

    }

    Action plunger(Closure<?> closure) {
        closureVal("plunger", MappedClosure.class, closure)
    }

    Action plunger(String name, Closure<?> closure) {
        closureVal("plunger", MappedClosure.class, closure)
        this.plunger.name = name
        this
    }

    Action plunger(String val) {
        closureVal("plunger", MappedClosure.class, { name val })
    }

    Action script(String script) {
        fieldVal("script", script)
    }

    Action inputText(String inputText) {
        fieldVal("inputText", inputText)
    }

    public String toPipelineScript(int tabsDepth) {
        def tabs = getTabs(tabsDepth)

        def lines = []

        if (script != null) {
            lines << "if (isUnix()) {"
            lines << "\tsh \"${script}\""
            lines << "else {"
            lines << "\tbat \"${script}\""
            lines << "}"
        } else if (inputText != null) {
            // TODO: Actually read up on input step so I can write it out here.
        } else if (!plunger.isEmpty()) {
            // If we don't have a name we're broken.
            if (plunger.containsKey("name")) {
                // if we've *only* got name, we've got simple output.
                if (plunger.size() == 1) {
                    // TODO: Write a runPlunger Pipeline step that actually does the loading and running of the plunger.
                    lines << "runPlunger \"${plunger.get('name')}\""
                } else {
                    // Otherwise, we're representing a closure!
                    lines << "runPlunger(\"${plunger.get('name')}\") {"
                    plunger.findAll { it.key != "name" }.each { k, v ->
                        lines << "\t${k} ${toArgForm(v)}"
                    }
                    lines << "}"
                }
            } else {
                // TODO: Error out! No name!
            }
        }

        return lines.collect { "${tabs}${it}" }.join("\n")
    }

    static final int serialVersionUID = 1L

}
