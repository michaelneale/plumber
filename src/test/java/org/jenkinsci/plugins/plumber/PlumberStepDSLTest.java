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
package org.jenkinsci.plugins.plumber;

import org.apache.commons.io.IOUtils;
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

import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertTrue;

public class PlumberStepDSLTest {
    @ClassRule
    public static BuildWatcher buildWatcher = new BuildWatcher();
    @Rule
    public RestartableJenkinsRule story = new RestartableJenkinsRule();
    @Rule public GitSampleRepoRule sampleRepo = new GitSampleRepoRule();
    @Rule public GitSampleRepoRule otherRepo = new GitSampleRepoRule();

    @Test
    public void testSingleSimpleStep() throws Exception {
        sampleRepo.init();
        sampleRepo.write("Jenkinsfile",
                pipelineSourceFromResources("singleSimpleStep"));

        sampleRepo.git("add", "Jenkinsfile");
        sampleRepo.git("commit", "--message=files");
        story.addStep(new Statement() {
            @Override public void evaluate() throws Throwable {
                WorkflowJob p = story.j.jenkins.createProject(WorkflowJob.class, "p");
                p.setDefinition(new CpsScmFlowDefinition(new GitStep(sampleRepo.toString()).createSCM(), "Jenkinsfile"));
                WorkflowRun b = p.scheduleBuild2(0).waitForStart();
                story.j.assertLogContains("hello",
                        story.j.assertBuildStatusSuccess(story.j.waitForCompletion(b)));


            }
        });
    }

    @Test
    public void testTwoLinearSteps() throws Exception {
        sampleRepo.init();
        sampleRepo.write("Jenkinsfile",
                pipelineSourceFromResources("twoLinearSteps"));

        sampleRepo.git("add", "Jenkinsfile");
        sampleRepo.git("commit", "--message=files");
        story.addStep(new Statement() {
            @Override public void evaluate() throws Throwable {
                WorkflowJob p = story.j.jenkins.createProject(WorkflowJob.class, "p");
                p.setDefinition(new CpsScmFlowDefinition(new GitStep(sampleRepo.toString()).createSCM(), "Jenkinsfile"));
                WorkflowRun b = p.scheduleBuild2(0).waitForStart();
                story.j.assertLogContains("hello",
                        story.j.assertBuildStatusSuccess(story.j.waitForCompletion(b)));
                story.j.assertLogNotContains("Multiple phase", b);
                story.j.assertLogContains("goodbye", b);


            }
        });
    }

    @Test
    public void testTwoParallelSteps() throws Exception {
        sampleRepo.init();
        sampleRepo.write("Jenkinsfile",
                pipelineSourceFromResources("twoParallelSteps"));

        sampleRepo.git("add", "Jenkinsfile");
        sampleRepo.git("commit", "--message=files");
        story.addStep(new Statement() {
            @Override public void evaluate() throws Throwable {
                WorkflowJob p = story.j.jenkins.createProject(WorkflowJob.class, "p");
                p.setDefinition(new CpsScmFlowDefinition(new GitStep(sampleRepo.toString()).createSCM(), "Jenkinsfile"));
                WorkflowRun b = p.scheduleBuild2(0).waitForStart();
                story.j.assertLogContains("onePhase",
                        story.j.assertBuildStatusSuccess(story.j.waitForCompletion(b)));
                story.j.assertLogContains("Multiple phase", b);
                story.j.assertLogContains("twoPhase", b);
            }
        });
    }

    @Test
    public void testCodeGen() throws Exception {
        sampleRepo.init();
        sampleRepo.write("Jenkinsfile",
                pipelineSourceFromResources("codeGen"));

        sampleRepo.git("add", "Jenkinsfile");
        sampleRepo.git("commit", "--message=files");
        story.addStep(new Statement() {
            @Override public void evaluate() throws Throwable {
                WorkflowJob p = story.j.jenkins.createProject(WorkflowJob.class, "p");
                p.setDefinition(new CpsScmFlowDefinition(new GitStep(sampleRepo.toString()).createSCM(), "Jenkinsfile"));
                WorkflowRun b = p.scheduleBuild2(0).waitForStart();
                story.j.assertLogContains("onePhase",
                        story.j.assertBuildStatusSuccess(story.j.waitForCompletion(b)));
                story.j.assertLogContains("Execute sub-workflows in parallel", b);
                story.j.assertLogContains("twoPhase", b);
            }
        });
    }

    @Test
    public void testArgsAsClosureFromNonCPS() throws Exception {
        sampleRepo.init();
        sampleRepo.write("Jenkinsfile",
                pipelineSourceFromResources("argsAsClosureFromNonCPS"));

        sampleRepo.git("add", "Jenkinsfile");
        sampleRepo.git("commit", "--message=files");
        story.addStep(new Statement() {
            @Override public void evaluate() throws Throwable {
                WorkflowJob p = story.j.jenkins.createProject(WorkflowJob.class, "p");
                p.setDefinition(new CpsScmFlowDefinition(new GitStep(sampleRepo.toString()).createSCM(), "Jenkinsfile"));
                WorkflowRun b = p.scheduleBuild2(0).waitForStart();
                story.j.assertLogContains("onePhase",
                        story.j.assertBuildStatusSuccess(story.j.waitForCompletion(b)));
                story.j.assertLogContains("Multiple phase", b);
                story.j.assertLogContains("twoPhase", b);
            }
        });
    }

    @Test
    public void testArgsAsMap() throws Exception {
        sampleRepo.init();
        sampleRepo.write("Jenkinsfile",
                pipelineSourceFromResources("argsAsMap"));

        sampleRepo.git("add", "Jenkinsfile");
        sampleRepo.git("commit", "--message=files");
        story.addStep(new Statement() {
            @Override public void evaluate() throws Throwable {
                WorkflowJob p = story.j.jenkins.createProject(WorkflowJob.class, "p");
                p.setDefinition(new CpsScmFlowDefinition(new GitStep(sampleRepo.toString()).createSCM(), "Jenkinsfile"));
                WorkflowRun b = p.scheduleBuild2(0).waitForStart();
                story.j.assertLogContains("onePhase",
                        story.j.assertBuildStatusSuccess(story.j.waitForCompletion(b)));
                story.j.assertLogContains("Multiple phase", b);
                story.j.assertLogContains("twoPhase", b);
            }
        });
    }

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
                WorkflowJob p = story.j.jenkins.createProject(WorkflowJob.class, "p");
                p.setDefinition(new CpsScmFlowDefinition(new GitStep(sampleRepo.toString()).createSCM(), "Jenkinsfile"));
                WorkflowRun b = p.scheduleBuild2(0).waitForStart();
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
                WorkflowJob p = story.j.jenkins.createProject(WorkflowJob.class, "p");
                p.setDefinition(new CpsScmFlowDefinition(new GitStep(sampleRepo.toString()).createSCM(), "Jenkinsfile"));
                WorkflowRun b = p.scheduleBuild2(0).waitForStart();
                story.j.assertLogContains("FIRST REPO",
                        story.j.assertBuildStatusSuccess(story.j.waitForCompletion(b)));
                story.j.assertLogContains("SECOND REPO", b);
            }
        });
    }

    @Test
    public void testArchive() throws Exception {
        sampleRepo.init();
        sampleRepo.write("Jenkinsfile",
                pipelineSourceFromResources("archive"));

        sampleRepo.git("add", "Jenkinsfile");
        sampleRepo.git("commit", "--message=files");
        story.addStep(new Statement() {
            @Override public void evaluate() throws Throwable {
                WorkflowJob p = story.j.jenkins.createProject(WorkflowJob.class, "p");
                p.setDefinition(new CpsScmFlowDefinition(new GitStep(sampleRepo.toString()).createSCM(), "Jenkinsfile"));
                WorkflowRun b = p.scheduleBuild2(0).waitForStart();
                story.j.assertLogContains("outputDir",
                        story.j.assertBuildStatusSuccess(story.j.waitForCompletion(b)));
                assertTrue(b.getArtifactManager().root().child("outputDir").isDirectory());
                assertTrue(b.getArtifactManager().root().child("outputDir").child("outputFile").isFile());
            }
        });
    }

    @Test
    public void testStashUnstash() throws Exception {
        sampleRepo.init();
        sampleRepo.write("Jenkinsfile",
                pipelineSourceFromResources("stashUnstash"));

        sampleRepo.git("add", "Jenkinsfile");
        sampleRepo.git("commit", "--message=files");
        story.addStep(new Statement() {
            @Override public void evaluate() throws Throwable {
                WorkflowJob p = story.j.jenkins.createProject(WorkflowJob.class, "p");
                p.setDefinition(new CpsScmFlowDefinition(new GitStep(sampleRepo.toString()).createSCM(), "Jenkinsfile"));
                WorkflowRun b = p.scheduleBuild2(0).waitForStart();
                story.j.assertLogContains("outputDir",
                        story.j.assertBuildStatusSuccess(story.j.waitForCompletion(b)));
                story.j.assertLogContains("PANTStrousers", b);
            }
        });
    }

    @Test
    public void testStashUnstashToDirectory() throws Exception {
        sampleRepo.init();
        sampleRepo.write("Jenkinsfile",
                pipelineSourceFromResources("stashUnstashToDirectory"));

        sampleRepo.git("add", "Jenkinsfile");
        sampleRepo.git("commit", "--message=files");
        story.addStep(new Statement() {
            @Override public void evaluate() throws Throwable {
                WorkflowJob p = story.j.jenkins.createProject(WorkflowJob.class, "p");
                p.setDefinition(new CpsScmFlowDefinition(new GitStep(sampleRepo.toString()).createSCM(), "Jenkinsfile"));
                WorkflowRun b = p.scheduleBuild2(0).waitForStart();
                story.j.assertLogContains("outputDir",
                        story.j.assertBuildStatusSuccess(story.j.waitForCompletion(b)));
                story.j.assertLogContains("PANTStrousers", b);
            }
        });
    }

    @Test
    public void testClean() throws Exception {
        sampleRepo.init();
        sampleRepo.write("Jenkinsfile",
                pipelineSourceFromResources("clean"));

        sampleRepo.git("add", "Jenkinsfile");
        sampleRepo.git("commit", "--message=files");
        story.addStep(new Statement() {
            @Override public void evaluate() throws Throwable {
                WorkflowJob p = story.j.jenkins.createProject(WorkflowJob.class, "p");
                p.setDefinition(new CpsScmFlowDefinition(new GitStep(sampleRepo.toString()).createSCM(), "Jenkinsfile"));
                WorkflowRun b = p.scheduleBuild2(0).waitForStart();
                story.j.assertBuildStatusSuccess(story.j.waitForCompletion(b));
            }
        });
    }

    @Test
    public void testArgsAsCpsClosure() throws Exception {
        sampleRepo.init();
        sampleRepo.write("Jenkinsfile",
                pipelineSourceFromResources("argsAsCpsClosure"));

        sampleRepo.git("add", "Jenkinsfile");
        sampleRepo.git("commit", "--message=files");
        story.addStep(new Statement() {
            @Override public void evaluate() throws Throwable {
                WorkflowJob p = story.j.jenkins.createProject(WorkflowJob.class, "p");
                p.setDefinition(new CpsScmFlowDefinition(new GitStep(sampleRepo.toString()).createSCM(), "Jenkinsfile"));
                WorkflowRun b = p.scheduleBuild2(0).waitForStart();
                story.j.assertLogContains("onePhase",
                        story.j.assertBuildStatusSuccess(story.j.waitForCompletion(b)));
                story.j.assertLogContains("Multiple phase", b);
                story.j.assertLogContains("twoPhase", b);
            }
        });

    }

    private String pipelineSourceFromResources(String pipelineName) throws IOException {
        String pipelineSource = null;

        URL url = getClass().getResource("/" + pipelineName + ".groovy");
        if (url != null) {
            pipelineSource = IOUtils.toString(url);
        }

        return pipelineSource;
    }
}
