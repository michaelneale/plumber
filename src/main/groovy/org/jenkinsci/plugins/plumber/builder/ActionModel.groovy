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
package org.jenkinsci.plugins.plumber.builder

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString
@EqualsAndHashCode
public class ActionModel extends AbstractPlumberModel {
    PlungerModel plunger
    String script
    String inputText

    public ActionModel(Map args) {
        this((PlungerModel) args.plunger,
                (String) args.script,
                (String) args.inputText)
    }

    public ActionModel(PlungerModel plunger = null, String script = null, String inputText = null) {
        if (plunger == null
                && script == null
                && inputText == null) {
            throw new IllegalArgumentException("One of 'plunger', 'script', or 'inputText' must be included and non-null.")
        }

        if ([plunger, script, inputText].count { it != null } > 1) {
            throw new IllegalArgumentException("No more than one of 'plunger', 'script', or 'inputText' may be set.")
        }

        this.plunger = plunger
        this.script = script
        this.inputText = inputText
    }
}
