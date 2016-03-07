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
import org.jenkinsci.plugins.plumber.Utils

@ToString
@EqualsAndHashCode
public class NotificationsModel extends AbstractPlumberModel {

    List<String> email

    // TODO: Decide whether to require type-checking etc for things like this. Also figure out how to make this easily
    // extensible...
    Closure hipchat
    Closure slack

    Boolean allPhases = true
    Boolean skipThisPhase = false
    Boolean onSuccess = false
    Boolean onFailure = true

    public NotificationsModel(Map args) {
        this(Utils.listForArg(args.email),
                (Closure) args.hipchat,
                (Closure) args.slack,
                (Boolean) args.allPhases,
                (Boolean) args.skipThisPhase,
                (Boolean) args.onSuccess,
                (Boolean) args.onFailure)
    }

    public NotificationsModel(List<String> email = null, Closure hipchat = null, Closure slack = null,
                              Boolean allPhases, Boolean skipThisPhase, Boolean onSuccess,
                              Boolean onFailure) {
        this.email = email
        this.hipchat = hipchat
        this.slack = slack

        this.allPhases = allPhases != null ?: true
        this.skipThisPhase = skipThisPhase != null ?: false
        this.onSuccess = onSuccess != null ?: false
        this.onFailure = onFailure != null ?: true
    }

}
