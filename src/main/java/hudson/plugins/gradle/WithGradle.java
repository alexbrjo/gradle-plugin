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

import com.google.common.collect.ImmutableSet;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.console.ConsoleLogFilter;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tools.ToolInstallation;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * The WithGradle pipeline step. Configures a Gradle installation and ConsoleAnnotator for a
 * Gradle build to run.
 *
 * @author Alex Johnson
 */
public class WithGradle extends Step {

    /** The Gradle installation to use */
    private GradleInstallation installation;

    @DataBoundConstructor
    public WithGradle () {

    }

    /**
     * Sets the globally configured gradle installation to set the GRADLE_HOME for
     * @param gradle the name of the installation to use
     */
    @DataBoundSetter
    public void setGradle (String gradle) {
        if (gradle == null) {
            return;
        }

        GradleInstallation[] installations = ToolInstallation.all().get(GradleInstallation.DescriptorImpl.class).getInstallations();
        for (GradleInstallation i : installations) {
            if (i.getName().equals(gradle)) {
                installation = i;
            }
        }
    }

    public GradleInstallation getGradle () {
        return installation;
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new WithGradleExecution(context, this);
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return new DescriptorImpl();
    }

    @Extension
    public static final class DescriptorImpl extends StepDescriptor {

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(Run.class, FilePath.class, TaskListener.class, EnvVars.class);
        }

        @Override
        public String getFunctionName() {
            return "withGradle";
        }

        @Override
        public String getDisplayName() {
            return "Builds with Gradle annotator";
        }

        @Override
        public boolean takesImplicitBlockArgument() {
            return true;
        }
    }

}
