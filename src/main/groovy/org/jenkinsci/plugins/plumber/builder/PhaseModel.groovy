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
public class PhaseModel extends AbstractPlumberModel {
    String name
    List<String> before
    List<String> after
    ActionModel action
    String label
    String dockerImage
    List<String> archiveDir
    List<String> stashDir
    List<UnstashModel> unstash
    NotificationsModel notifications

    public PhaseModel(Map args) {
        this((String) args.name,
                (ActionModel) args.action,
                Utils.listForArg(args.before),
                Utils.listForArg(args.after),
                (String) args.label,
                (String) args.dockerImage,
                Utils.listForArg(args.archiveDir),
                Utils.listForArg(args.stashDir),
                (List<UnstashModel>) args.unstash,
                (NotificationsModel) args.notifications)
    }

    public PhaseModel(@Nonnull String name,
                      @Nonnull ActionModel action,
                      List<String> before,
                      List<String> after,
                      String label,
                      String dockerImage,
                      List<String> archiveDir,
                      List<String> stashDir,
                      List<UnstashModel> unstash,
                      NotificationsModel notifications) {
        if (label != null && dockerImage != null) {
            throw new IllegalArgumentException("Only one of 'label' or 'dockerImage' may be specified in a phase.")
        }
        this.name = name
        this.before = before
        this.after = after
        this.action = action
        this.label = label
        this.dockerImage = dockerImage
        this.archiveDir = archiveDir
        this.stashDir = stashDir
        this.unstash = unstash
        this.notifications = notifications
    }


}
