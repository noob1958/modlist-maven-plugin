package com.github.noob1958.modlist.maven.plugin;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugins.annotations.Parameter;
import org.junit.Rule;
import org.junit.Test;
import java.io.File;

import static org.junit.Assert.assertNotNull;

@SuppressWarnings("deprecation")
public class ModlistMojoTest {

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession session;

    @Rule
    public MojoRule rule = new MojoRule();

    @Test
    public void testMojoExecution() throws Exception {
        File pom = new File("src/test/resources/project-to-test");
        // Now rule.before() has been called by JUnit, so mojoDescriptors is initialized
        ModlistMojo mojo = rule.lookupConfiguredMojo(pom, "list");
        assertNotNull(mojo);
        mojo.execute();
    }
}
