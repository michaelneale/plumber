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

import javax.annotation.Nonnull

@ToString
@EqualsAndHashCode
public class RootModel extends AbstractPlumberModel {

    List<PhaseModel> phases
    Map<String,String> env
    NotificationsModel notifications
    List<String> archiveDir
    List<String> stashDir

    public RootModel(Map args) {
        this((List<PhaseModel>) args.phases,
                (Map<String,String>) args.env,
                (NotificationsModel) args.notifications,
                Utils.listForArg(args.archiveDir),
                Utils.listForArg(args.stashDir)
        )
    }

    public RootModel(@Nonnull List<PhaseModel> phases = null,
                     Map<String, String> env = null,
                     NotificationsModel notifications = null,
                     List<String> archiveDir = null,
                     List<String> stashDir = null) {
        if (phases.isEmpty()) {
            throw new IllegalArgumentException("One or more 'phases' must be specified.")
        }
        this.phases = phases
        this.env = env
        this.notifications = notifications
        this.archiveDir = archiveDir
        this.stashDir = stashDir
    }
}
