package hudson.plugins.gradle;

import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerFixture;

/**
 * Runs Gradle container.
 *
 * @author Alex Johnson
 */
@DockerFixture(id="gradle", ports=8080)
public class GradleContainer extends DockerContainer {

}