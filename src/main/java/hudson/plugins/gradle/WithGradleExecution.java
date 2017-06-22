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

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.console.ConsoleLogFilter;
import hudson.model.AbstractBuild;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.workflow.steps.*;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.HashMap;

/**
 * The execution of the {@link WithGradle} pipeline step
 *
 * @author Alex Johnson
 */
public class WithGradleExecution extends StepExecution {

    /** The Step and it's required context */
    private transient WithGradle step;
    private transient FilePath workspace;
    private transient Run run;
    private transient TaskListener listener;
    private transient EnvVars envVars;

    private BodyExecution block;

    public WithGradleExecution (StepContext context, WithGradle step) throws IOException, InterruptedException {
        super(context);
        this.step = step;

        workspace = context.get(FilePath.class);
        run = context.get(Run.class);
        listener = context.get(TaskListener.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean start() throws Exception {
        envVars = new EnvVars();

        GradleInstallation gradle = step.getGradle();
        if (gradle != null) {
            envVars.put("GRADLE_HOME", gradle.getHome());
        }

        ConsoleLogFilter annotator = BodyInvoker.mergeConsoleLogFilters(getContext().get(ConsoleLogFilter.class), new GradleConsoleFilter());
        EnvironmentExpander expander = EnvironmentExpander.merge(getContext().get(EnvironmentExpander.class), new GradleExpander(envVars));

        if (getContext().hasBody()) {
           block = getContext().newBodyInvoker().withContexts(annotator, expander).start();
        }
        getContext().onSuccess(Result.SUCCESS);
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop(@Nonnull Throwable cause) throws Exception {

    }

    /**
     * Wraps {@link GradleConsoleAnnotator} in a {@link ConsoleLogFilter} so it can be merged with the existing
     * log filter.
     */
    private static class GradleConsoleFilter extends ConsoleLogFilter implements Serializable {

        private static final long serialVersionUID = 1;

        public GradleConsoleFilter() {
        }

        /**
         * Creates a {@link GradleConsoleAnnotator} for an {@link OutputStream}
         *
         * @param build this is ignored
         * @param out the {@link OutputStream} to annotate
         * @return the {@link GradleConsoleAnnotator} for the OutputStream
         */
        @Override
        public OutputStream decorateLogger(AbstractBuild build, final OutputStream out) {
            return new GradleConsoleAnnotator(out, Charset.forName("UTF-8"));
        }
    }

    /**
     * Overrides the existing environment with the pipeline Gradle settings
     */
    private static final class GradleExpander extends EnvironmentExpander {

        private static final long serialVersionUID = 1;

        private final EnvVars gradleEnv;

        private GradleExpander(EnvVars env) {
            this.gradleEnv = new EnvVars();
            gradleEnv.putAll(env);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void expand(EnvVars env) throws IOException, InterruptedException {
            env.overrideAll(gradleEnv);
        }
    }

}
