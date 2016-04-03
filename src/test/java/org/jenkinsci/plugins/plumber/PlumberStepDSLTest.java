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

import hudson.model.Label;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
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
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.RestartableJenkinsRule;

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
                "plumber \"\"\"\n"
                        + "  debug true\n"
                        + "  phase {\n"
                        + "    name 'pants'\n"
                        + "    action {\n"
                        + "      script 'echo hello'\n"
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
                story.j.assertLogContains("hello",
                        story.j.assertBuildStatusSuccess(story.j.waitForCompletion(b)));


            }
        });
    }

    @Test
    public void testTwoLinearSteps() throws Exception {
        sampleRepo.init();
        sampleRepo.write("Jenkinsfile",
                "plumber \"\"\"\n"
                        + "  debug true\n"
                        + "  phase {\n"
                        + "    name 'pants'\n"
                        + "    action {\n"
                        + "      script 'echo hello'\n"
                        + "    }\n"
                        + "  }\n"
                        + "  phase {\n"
                        + "    name 'trousers'\n"
                        + "    action {\n"
                        + "      script 'echo goodbye'\n"
                        + "    }\n"
                        + "    after 'pants'\n"
                        + "  }\n"
                        + "\"\"\"\n");

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
                "plumber \"\"\"\n"
                        + "  debug true\n"
                        + "  phase {\n"
                        + "    name 'pants'\n"
                        + "    action {\n"
                        + "      script 'echo onePhase'\n"
                        + "    }\n"
                        + "  }\n"
                        + "  phase {\n"
                        + "    name 'trousers'\n"
                        + "    action {\n"
                        + "      script 'echo twoPhase'\n"
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
                "plumber(\"\"\"\n"
                        + "  debug true\n"
                        + "  phase {\n"
                        + "    name 'pants'\n"
                        + "    action {\n"
                        + "      script 'echo onePhase'\n"
                        + "    }\n"
                        + "  }\n"
                        + "  phase {\n"
                        + "    name 'trousers'\n"
                        + "    action {\n"
                        + "      script 'echo twoPhase'\n"
                        + "    }\n"
                        + "  }\n"
                        + "\"\"\", true)\n");

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
                "@NonCPS\n"
                        + "def plumberConf() {\n"
                        + "  return {\n"
                        + "    debug true\n"
                        + "    phase {\n"
                        + "      name 'pants'\n"
                        + "      action {\n"
                        + "        script 'echo onePhase'\n"
                        + "      }\n"
                        + "    }\n"
                        + "    phase {\n"
                        + "      name 'trousers'\n"
                        + "      action {\n"
                        + "        script 'echo twoPhase'\n"
                        + "      }\n"
                        + "    }\n"
                        + "  }\n"
                        + "}\n\n"
                        + "plumber(plumberConf(), false)\n");

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
                "plumber([\n"
                        + "  debug: true,\n"
                        + "  phases: [\n"
                        + "    [\n"
                        + "      name: 'pants',\n"
                        + "      action: [\n"
                        + "        script: 'echo onePhase'\n"
                        + "      ]\n"
                        + "    ]\n,"
                        + "    [\n"
                        + "      name: 'trousers',\n"
                        + "      action: [\n"
                        + "        script: 'echo twoPhase'\n"
                        + "      ]\n"
                        + "    ]\n"
                        + "  ]\n"
                        + "])\n");

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
                "plumber([\n"
                        + "  debug: true,\n"
                        + "  scm: [\n"
                        + "    [\n"
                        + "      name: 'git',\n"
                        + "      config: [\n"
                        + "        url: $/" + otherRepo + "/$,\n"
                        + "        branch: '*/master'\n"
                        + "      ]\n"
                        + "    ]\n"
                        + "  ],\n"
                        + "  phases: [\n"
                        + "    [\n"
                        + "      name: 'pants',\n"
                        + "      action: [\n"
                        + "        name: 'catFile',\n"
                        + "        file: 'README'\n"
                        + "      ]\n"
                        + "    ]\n"
                        + "  ]\n"
                        + "])\n");

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
                "plumber([\n"
                        + "  debug: true,\n"
                        + "  phases: [\n"
                        + "    [\n"
                        + "      name: 'first',\n"
                        + "      action: [\n"
                        + "        name: 'catFile',\n"
                        + "        file: 'README'\n"
                        + "      ]\n"
                        + "    ],\n"
                        + "    [\n"
                        + "      name: 'second',\n"
                        + "      action: [\n"
                        + "        name: 'catFile',\n"
                        + "        file: 'README'\n"
                        + "      ],\n"
                        + "      scm: [\n"
                        + "        [\n"
                        + "          name: 'git',\n"
                        + "          config: [\n"
                        + "            url: $/" + otherRepo + "/$,\n"
                        + "            branch: '*/master'\n"
                        + "          ]\n"
                        + "        ]\n"
                        + "      ],\n"
                        + "      after: 'first',\n"
                        + "    ]\n"
                        + "  ]\n"
                        + "])\n");

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
                "plumber([\n"
                        + "  debug: true,\n"
                        + "  archiveDirs: ['outputDir/**'],"
                        + "  phases: [\n"
                        + "    [\n"
                        + "      name: 'pants',\n"
                        + "      action: [\n"
                        + "        script: 'mkdir -p outputDir; echo \"onePhase\" > outputDir/outputFile'\n"
                        + "      ]\n"
                        + "    ]\n"
                        + "  ]\n"
                        + "])\n");

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
                "plumber([\n"
                        + "  debug: true,\n"
                        + "  phases: [\n"
                        + "    [\n"
                        + "      name: 'pants',\n"
                        + "      stashDirs: ['outputDir/**'],"
                        + "      action: [\n"
                        + "        script: 'mkdir -p outputDir; echo \"trousers\" > outputDir/outputFile'\n"
                        + "      ]\n"
                        + "    ],\n"
                        + "    [\n"
                        + "      name: 'shirts',\n"
                        + "      unstash: 'pants',\n"
                        + "      action: [\n"
                        + "        script: 'cat outputDir/outputFile'\n"
                        + "      ],\n"
                        + "      after: 'pants',\n"
                        + "    ]\n"
                        + "  ]\n"
                        + "])\n");

        sampleRepo.git("add", "Jenkinsfile");
        sampleRepo.git("commit", "--message=files");
        story.addStep(new Statement() {
            @Override public void evaluate() throws Throwable {
                WorkflowJob p = story.j.jenkins.createProject(WorkflowJob.class, "p");
                p.setDefinition(new CpsScmFlowDefinition(new GitStep(sampleRepo.toString()).createSCM(), "Jenkinsfile"));
                WorkflowRun b = p.scheduleBuild2(0).waitForStart();
                story.j.assertLogContains("outputDir",
                        story.j.assertBuildStatusSuccess(story.j.waitForCompletion(b)));
                story.j.assertLogContains("trousers", b);
            }
        });
    }
}
