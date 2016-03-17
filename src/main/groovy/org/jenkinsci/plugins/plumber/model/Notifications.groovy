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

}
