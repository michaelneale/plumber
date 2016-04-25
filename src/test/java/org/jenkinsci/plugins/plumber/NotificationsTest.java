/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jenkinsci.plugins.plumber;

import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Test;
import org.junit.runners.model.Statement;

public class NotificationsTest extends AbstractPlumberTest {

    @Test
    public void testNotificationsDefaults() throws Exception {
        prepRepoWithJenkinsfile("notificationsDefaults");

        story.addStep(new Statement() {
            @Override public void evaluate() throws Throwable {
                WorkflowRun b = getAndStartBuild();
                story.j.assertLogContains("NOT_FOUND",
                        story.j.assertBuildStatusSuccess(story.j.waitForCompletion(b)));
            }
        });

    }

    @Test
    public void testNotificationsOnSuccess() throws Exception {
        prepRepoWithJenkinsfile("notificationsOnSuccess");

        story.addStep(new Statement() {
            @Override public void evaluate() throws Throwable {
                WorkflowRun b = getAndStartBuild();
                story.j.assertLogContains("name:echoToFileNotifier",
                        story.j.assertBuildStatusSuccess(story.j.waitForCompletion(b)));
                story.j.assertLogContains("file:notifyOutput", b);
                story.j.assertLogContains("phaseName:pants", b);
                story.j.assertLogContains("before:false", b);
                story.j.assertLogContains("buildInfo:p#1", b);
                story.j.assertLogContains("result:SUCCESS", b);
            }
        });

    }

    @Test
    public void testNotificationsBeforePhase() throws Exception {
        prepRepoWithJenkinsfile("notificationsBeforePhase");

        story.addStep(new Statement() {
            @Override public void evaluate() throws Throwable {
                WorkflowRun b = getAndStartBuild();
                story.j.assertLogContains("name:echoToFileNotifier",
                        story.j.assertBuildStatusSuccess(story.j.waitForCompletion(b)));
                story.j.assertLogContains("file:notifyOutput", b);
                story.j.assertLogContains("phaseName:pants", b);
                story.j.assertLogContains("before:true", b);
                story.j.assertLogContains("buildInfo:p#1", b);
                story.j.assertLogContains("result:SUCCESS", b);
            }
        });
    }
}
