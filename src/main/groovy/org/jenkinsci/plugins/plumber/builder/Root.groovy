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
public class Root extends AbstractPlumberModel {

    List<Phase> phases = []
    Map<String,String> env = [:]
    Notifications notifications
    List<String> archiveDirs = []
    List<String> stashDirs = []

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
}
