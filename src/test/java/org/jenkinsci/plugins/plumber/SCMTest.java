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

public class SCMTest extends AbstractPlumberTest {
    @Test
    public void testBasicCloneAndUpdate() throws Exception {
        otherRepo.init();
        otherRepo.write("README",
                "THIS IS A README");
        otherRepo.git("add", "README");
        otherRepo.git("commit", "--message=files");

        sampleRepo.init();
        sampleRepo.write("Jenkinsfile",
                pipelineSourceFromResources("basicCloneAndUpdate").replaceAll("REPO_URL_TOKEN", otherRepo.toString()));

        sampleRepo.git("add", "Jenkinsfile");
        sampleRepo.git("commit", "--message=files");
        story.addStep(new Statement() {
            @Override public void evaluate() throws Throwable {
                WorkflowRun b = getAndStartBuild();
                story.j.assertLogContains("THIS IS A README",
                        story.j.assertBuildStatusSuccess(story.j.waitForCompletion(b)));
            }
        });
    }

    @Test
    public void testSCMPhaseOverride() throws Exception {
        otherRepo.init();
        otherRepo.write("README",
                "SECOND REPO");
        otherRepo.git("add", "README");
        otherRepo.git("commit", "--message=files");

        sampleRepo.init();
        sampleRepo.write("README",
                "FIRST REPO");
        sampleRepo.git("add", "README");

        sampleRepo.write("Jenkinsfile",
                pipelineSourceFromResources("SCMPhaseOverride").replaceAll("REPO_URL_TOKEN", otherRepo.toString()));


        sampleRepo.git("add", "Jenkinsfile");
        sampleRepo.git("commit", "--message=files");
        story.addStep(new Statement() {
            @Override public void evaluate() throws Throwable {
                WorkflowRun b = getAndStartBuild();
                story.j.assertLogContains("FIRST REPO",
                        story.j.assertBuildStatusSuccess(story.j.waitForCompletion(b)));
                story.j.assertLogContains("SECOND REPO", b);
            }
        });
    }


}
