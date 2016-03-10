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

    Phase after(String val) {
        addValToList("after", val)
    }

    Phase after(List<String> val) {
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
        closureVal("action", Action.getClass(), closure)
    }

    Phase notifications(Closure<?> closure) {
        closureVal("notifications", Notifications.getClass(), closure)
    }

    Phase unstash(Closure<?> closure) {
        addClosureValToList("unstash", Unstash.getClass(), closure)
    }

    Phase unstash(String from, String dir) {
        addValToList("unstash", new Unstash().fromPhase(from).dir(dir))
    }
}
