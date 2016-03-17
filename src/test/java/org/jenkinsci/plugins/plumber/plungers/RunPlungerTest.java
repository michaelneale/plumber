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
package org.jenkinsci.plugins.plumber.plungers;

import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.steps.scm.GitSampleRepoRule;
import org.jenkinsci.plugins.workflow.steps.scm.GitStep;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.model.Statement;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.RestartableJenkinsRule;

public class RunPlungerTest {
    @ClassRule
    public static BuildWatcher buildWatcher = new BuildWatcher();
    @Rule
    public RestartableJenkinsRule story = new RestartableJenkinsRule();
    @Rule public GitSampleRepoRule sampleRepo = new GitSampleRepoRule();

    @Test
    public void testSimpleEchoPlunger() throws Exception {
        sampleRepo.init();
        sampleRepo.write("Jenkinsfile",
                "plumber \"\"\"\n"
                        + "  debug true\n"
                        + "  phase {\n"
                        + "    name 'pants'\n"
                        + "    action {\n"
                        + "      plunger('simpleEcho') {\n"
                        + "        pants 'trousers'\n"
                        + "        shirts 'polos'\n"
                        + "      }\n"
                        + "    }\n"
                        + "  }\n"
                        + "\"\"\"\n");

        sampleRepo.git("add", "Jenkinsfile");
        sampleRepo.git("commit", "--message=files");
        story.addStep(new Statement() {
            @Override public void evaluate() throws Throwable {
                WorkflowJob p = story.j.jenkins.createProject(WorkflowJob.class, "p");
                p.setDefinition(new CpsScmFlowDefinition(new GitStep(sampleRepo.toString()).createSCM(), "Jenkinsfile"));
                WorkflowRun b = p.scheduleBuild2(0).waitForStart();
                story.j.assertLogContains("echoing pants == trousers",
                        story.j.assertBuildStatusSuccess(story.j.waitForCompletion(b)));
                story.j.assertLogContains("echoing shirts == polos", b);

            }
        });
    }

}
