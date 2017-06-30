/*
 * The MIT License
 *
 * Copyright (c) 2017, CloudBees, Inc.
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
package hudson.plugins.gradle;

import hudson.model.Node;
import hudson.model.Result;
import hudson.slaves.DumbSlave;
import hudson.slaves.NodeProperty;
import hudson.slaves.RetentionStrategy;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import hudson.plugins.sshslaves.SSHLauncher;
import org.jenkinsci.test.acceptance.docker.DockerRule;
import org.jenkinsci.test.acceptance.junit.WithDocker;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.model.Statement;
import org.jvnet.hudson.test.RestartableJenkinsRule;

import java.util.Collections;

/**
 * Tests the withGradle pipeline step
 *
 * @author Alex Johnson
 */
@WithDocker
public class WithGradleWorkFlowTest {

    @Rule
    public RestartableJenkinsRule r = new RestartableJenkinsRule();

    @Rule
    public DockerRule<GradleContainer> gradleSlave = new DockerRule<GradleContainer>(GradleContainer.class);

    private String build_gradle = "writeFile(file:'build.gradle', text:'defaultTasks \\\'hello\\\'\\ntask hello << { println \\\'Hello\\\' }') \n";

    /*
        TODO DO NOT MERGE: when jenkins-test-harness-tools 2.1 is released add GradleInstallation tool. Right now
        these tests use the system's default Gradle. If Gradle is not installed these tests will fail.

        TODO: convert these tests to Groovy OR create src/test/java/.../WithGradleWorkFlowTest.java
        TODO? add test for console annotation
        TODO? add test for reloading annotator on Jenkins restart
    */

    @Test
    public void testGradle () throws Exception {
        r.addStep(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                GradleContainer slave = gradleSlave.get();
                // 0 executors so build has to run on slave
                r.j.jenkins.setNumExecutors(0);
                // SSH launcher to Docker container
                r.j.jenkins.addNode(new DumbSlave("remote", "", "/home/test/slave", "1", Node.Mode.NORMAL, "",
                        new SSHLauncher(slave.ipBound(8080), slave.port(8080), "test", "test", "", ""),
                        RetentionStrategy.INSTANCE, Collections.<NodeProperty<?>>emptyList()));
                // Run the gradle build
                WorkflowJob p1 = r.j.jenkins.createProject(WorkflowJob.class, "FakeProject");
                p1.setDefinition(new CpsFlowDefinition("node {\n" + build_gradle + "withGradle {\n sh 'gradle'\n}\n}", false));
                WorkflowRun run = r.j.assertBuildStatus(Result.SUCCESS, p1.scheduleBuild2(0));
            }
        });
    }

    @Test
    public void testStepDefaultTools() throws Exception {
        WorkflowJob p1 = r.j.jenkins.createProject(WorkflowJob.class, "FakeProject");
        p1.setDefinition(new CpsFlowDefinition("node {\n" + build_gradle +
                "withGradle {\n" +
                "sh 'gradle'\n" + // runs default task
                "}\n" +
                "}", false));
        WorkflowRun run = p1.scheduleBuild2(0).get();
        r.j.assertBuildStatusSuccess(run);
    }

    @Test
    public void testGradleErrorFailsBuild() throws Exception {
        WorkflowJob p1 = r.j.jenkins.createProject(WorkflowJob.class, "FakeProject");
        p1.setDefinition(new CpsFlowDefinition("node {\n" + build_gradle +
                "withGradle {\n" +
                "sh 'gradle unknownTask'\n" +
                "}\n" +
                "}", false));
        WorkflowRun run = p1.scheduleBuild2(0).get();
        r.j.assertBuildStatus(Result.FAILURE, run);
    }

    @Test
    public void testStepWithConfiguredGradle() throws Exception {
        WorkflowJob p1 = r.j.jenkins.createProject(WorkflowJob.class, "FakeProject");
        p1.setDefinition(new CpsFlowDefinition("node {\n" + build_gradle +
                "withGradle (gradle: 'g2') {\n" +
                "sh 'gradle'\n" +
                "}\n" +
                "}", false));
        WorkflowRun run = p1.scheduleBuild2(0).get();
        r.j.assertBuildStatusSuccess(run);
    }
}
