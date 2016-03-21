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
public class Unstash extends AbstractPlumberModel {
    String fromPhase
    String dir

    Unstash fromPhase(String val) {
        fieldVal("fromPhase", val)
    }

    Unstash dir(String val) {
        fieldVal("dir", val)
    }

    public List<String> toPipelineScript(int tabsDepth) {
        def tabs = getTabs(tabsDepth)

        def lines = []

        // Just realized we don't actually have a directory parameter to unstash yet. I feel silly.
        // Keeping the class and such as is for the moment anyway.
        // TODO: Either add output directory support to unstash or get rid of this pointless class!
        lines << "unstash ${toArgForm(fromPhase)}"
        return lines.collect { "${tabs}${it}" }
    }

    static final int serialVersionUID = 1L

}
