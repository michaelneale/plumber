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

public class MatrixTest extends AbstractPlumberTest {
    @Test
    public void testSimpleMatrix() throws Exception {
        prepRepoWithJenkinsfile("simpleMatrix");

        story.addStep(new Statement() {
            @Override public void evaluate() throws Throwable {
                WorkflowRun b = getAndStartBuild();
                story.j.assertLogContains("FOO is bar",
                        story.j.assertBuildStatusSuccess(story.j.waitForCompletion(b)));
                story.j.assertLogContains("FOO is baz", b);
                story.j.assertLogContains("Multiple phases in an execution set, run in parallel", b);
                story.j.assertLogContains("[echo-phase+FOO=bar]", b);
                story.j.assertLogContains("[echo-phase+FOO=baz]", b);
            }
        });
    }

    @Test
    public void testSingleAxisSingleValueMatrix() throws Exception {
        prepRepoWithJenkinsfile("singleAxisSingleValueMatrix");

        story.addStep(new Statement() {
            @Override public void evaluate() throws Throwable {
                WorkflowRun b = getAndStartBuild();
                story.j.assertLogContains("FOO is bar",
                        story.j.assertBuildStatusSuccess(story.j.waitForCompletion(b)));
                story.j.assertLogContains("Single phase in an execution set, run alone", b);
            }
        });
    }

    @Test
    public void testDoubleAxisSingleValueMatrix() throws Exception {
        prepRepoWithJenkinsfile("doubleAxisSingleValueMatrix");

        story.addStep(new Statement() {
            @Override public void evaluate() throws Throwable {
                WorkflowRun b = getAndStartBuild();
                story.j.assertLogContains("FOO is bar and PANTS is trousers",
                        story.j.assertBuildStatusSuccess(story.j.waitForCompletion(b)));
                story.j.assertLogContains("Single phase in an execution set, run alone", b);
            }
        });
    }

    @Test
    public void testDoubleAxisDoubleValueMatrix() throws Exception {
        prepRepoWithJenkinsfile("doubleAxisDoubleValueMatrix");

        story.addStep(new Statement() {
            @Override public void evaluate() throws Throwable {
                WorkflowRun b = getAndStartBuild();
                story.j.assertLogContains("FOO is bar and PANTS is trousers",
                        story.j.assertBuildStatusSuccess(story.j.waitForCompletion(b)));
                story.j.assertLogContains("Multiple phases in an execution set, run in parallel", b);
                story.j.assertLogContains("[echo-phase+FOO=bar,PANTS=trousers]", b);
                story.j.assertLogContains("[echo-phase+FOO=bar,PANTS=slacks]", b);
                story.j.assertLogContains("[echo-phase+FOO=baz,PANTS=trousers]", b);
                story.j.assertLogContains("[echo-phase+FOO=baz,PANTS=slacks]", b);
                story.j.assertLogContains("FOO is bar and PANTS is slacks", b);
                story.j.assertLogContains("FOO is baz and PANTS is trousers", b);
                story.j.assertLogContains("FOO is baz and PANTS is slacks", b);

            }
        });
    }

}
