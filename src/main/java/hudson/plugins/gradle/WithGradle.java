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
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Set;

/**
 * @author Alex Johnson
 */
public class WithGradle extends Gradle implements SimpleBuildStep {

    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath filePath, @Nonnull Launcher launcher, @Nonnull TaskListener taskListener) throws InterruptedException, IOException {
        perform((AbstractBuild) run, launcher, (BuildListener) taskListener);
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Symbol("withGradle")
    @Extension
    public static final class DescriptorImpl extends Gradle.DescriptorImpl {

        @Override
        public boolean isApplicable(Class jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "withGradle";
        }
    }

    /*public static final class ExecutionImpl extends SynchronousNonBlockingStepExecution<Void> {

        private transient final WithGradle step;
        // The serial ID
        private static final long serialVersionUID = 1L;

        ExecutionImpl(WithGradle step, StepContext context) {
            super(context);
            this.step = step;
        }

        @Override
        protected Void run() throws Exception {
            Run r = getContext().get(Run.class);
            if (r instanceof AbstractBuild) {
                Launcher launcher = getContext().get(Launcher.class);
                BuildListener listener = (BuildListener)getContext().get(TaskListener.class);
                step.perform((AbstractBuild)r, launcher, listener);
            }
            return null;
        }

    }*/
}
