package com.github.noob1958.modlist.maven.plugin;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import javax.inject.Inject;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * <p>Goal which reads the names of all modules in a folder into a maven session property.</p>
 * <p>Usage:</p>
 * Add these tags to the plugin's configuration.
 * <ul>
 *     <li>inputDir: the path to the folder to read, relative to the project's base directory. This tag is mandatory.</li>
 *     <li>outputPropertyName: the name of the session property that will receive the output. Optional; the default is "modlist".</li>
 * </ul>
 */
@Mojo(name = "list")
public class ModlistMojo extends AbstractMojo {
    /**
     * The path to the folder to read, relative to the project's base directory. This tag is mandatory.
     */
    @Parameter(name = "inputDir", property = "inputDir", required = true)
    private String inputDir;

    /**
     * The name of the session property that will receive the output. Optional; the default is "modlist".
     */
    @Parameter(name = "outputPropertyName", property = "outputPropertyName", defaultValue = "modlist")
    private String outputPropertyName;

    private final MavenSession session;
    private final MavenProject project;
    private final MojoExecution mojoExecution;

    @Inject
    public ModlistMojo(MavenSession session, MavenProject project, MojoExecution mojoExecution) {
        this.session = session;
        this.project = project;
        this.mojoExecution = mojoExecution;
    }

    /**
     * Reads a jar and extracts the full module name. Any runtime exceptions are swallowed.
     * @param jarPath the path, must be absolute
     * @return the name, or an empty String in case something went wrong
     */
    private String fullModuleName(Path jarPath) {
        try {
            // ModuleFinder scans the specific JAR file:
            ModuleFinder finder = ModuleFinder.of(jarPath);
            // Find the first (and only) module in that JAR:
            Optional<ModuleReference> omr = finder.findAll().stream().findFirst();
            if (omr.isPresent()) {
                return omr.get().descriptor().name();
            } else {
                getLog().warn("Skipped, not a valid JAR or module: " + jarPath.getFileName());
                return "";
            }
        } catch (RuntimeException e) {
            getLog().error("Skipped because of a problem while reading the file: "+jarPath.getFileName());
            return "";
        }
    }

    @Override
    public void execute() throws MojoExecutionException {
        if (inputDir==null || inputDir.isBlank()) throw new MojoExecutionException("inputDir must not be empty");
        Path baseDir = project.getBasedir().toPath();
        Path inputDirPath = baseDir.resolve(inputDir);
        if (!Files.isDirectory(inputDirPath)) throw new MojoExecutionException("Directory not found: " + inputDir);
        getLog().info("Reading directory "+inputDirPath);
        StringBuilder result = new StringBuilder();
        try (var stream = Files.list(inputDirPath)) {
            List<Path> list = stream.toList();
            String name;
            for (Path path : list) {
                // fully qualified name
                name = fullModuleName(path);
                if (!name.isBlank()) {
                    if (!result.isEmpty()) {
                        result.append(",");
                    }
                    result.append(name);
                }
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Unable to read directory "+inputDirPath, e);
        }
        session.getUserProperties().setProperty(this.outputPropertyName, result.toString());
        getLog().info("Stored the following in maven session property "+this.outputPropertyName+": "+result);
    }

}
