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

import static org.junit.Assert.assertTrue;

public class StashUnstashArchiveTest extends AbstractPlumberTest {
    @Test
    public void testStashUnstashToDirectoryCPSClosure() throws Exception {
        prepRepoWithJenkinsfile("stashUnstashToDirectoryCPSClosure");

        story.addStep(new Statement() {
            @Override public void evaluate() throws Throwable {
                WorkflowRun b = getAndStartBuild();
                story.j.assertLogContains("outputDir",
                        story.j.assertBuildStatusSuccess(story.j.waitForCompletion(b)));
                story.j.assertLogContains("PANTStrousers", b);
            }
        });
    }

    @Test
    public void testArchive() throws Exception {
        prepRepoWithJenkinsfile("archive");

        story.addStep(new Statement() {
            @Override public void evaluate() throws Throwable {
                WorkflowRun b = getAndStartBuild();
                story.j.assertLogContains("outputDir",
                        story.j.assertBuildStatusSuccess(story.j.waitForCompletion(b)));
                assertTrue(b.getArtifactManager().root().child("outputDir").isDirectory());
                assertTrue(b.getArtifactManager().root().child("outputDir").child("outputFile").isFile());
            }
        });
    }

    @Test
    public void testStashUnstash() throws Exception {
        prepRepoWithJenkinsfile("stashUnstash");

        story.addStep(new Statement() {
            @Override public void evaluate() throws Throwable {
                WorkflowRun b = getAndStartBuild();
                story.j.assertLogContains("outputDir",
                        story.j.assertBuildStatusSuccess(story.j.waitForCompletion(b)));
                story.j.assertLogContains("PANTStrousers", b);
            }
        });
    }

    @Test
    public void testStashUnstashToDirectory() throws Exception {
        prepRepoWithJenkinsfile("stashUnstashToDirectory");

        story.addStep(new Statement() {
            @Override public void evaluate() throws Throwable {
                WorkflowRun b = getAndStartBuild();
                story.j.assertLogContains("outputDir",
                        story.j.assertBuildStatusSuccess(story.j.waitForCompletion(b)));
                story.j.assertLogContains("PANTStrousers", b);
            }
        });
    }

}
