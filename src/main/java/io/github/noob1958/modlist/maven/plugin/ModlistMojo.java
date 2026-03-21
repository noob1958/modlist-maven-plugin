package io.github.noob1958.modlist.maven.plugin;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.Optional;

/**
 * <p>Goal which reads the names of all modules in a folder into a maven session property. Modules in subfolders are
 * visited as well.</p>
 * <p>Add these tags to the plugin's configuration.</p>
 * <ul>
 *     <li>inputDir: the path to the folder to read, relative to the project's base directory. This tag is mandatory.</li>
 *     <li>outputPropertyName: the name of the session property that will receive the output. Optional; the default is "modlist".</li>
 *     <li>excludeFiles: An optional glob-format string indicating which of the files in the inputDir to exclude before generating
 *      the module list. E.g. "abc-*-def.jar" will exclude "abc-ghi-def.jar".</li>
 *     <li>deleteFiles: An optional glob-format string indicating which of the files in the inputDir to delete before generating
 *      the module list. E.g. "abc-*-def.jar" will delete "abc-ghi-def.jar".</li>
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

    /**
     * An optional glob-format string indicating which of the files in the inputDir to exclude before generating
     * the module list. E.g. "abc-*-def.jar" will exclude "abc-ghi-def.jar".
     */
    @Parameter(name = "excludeFiles", property = "excludeFiles")
    private String excludeFiles;

    /**
     * An optional glob-format string indicating which of the files in the inputDir to delete before generating
     * the module list. E.g. "abc-*-def.jar" will delete "abc-ghi-def.jar".
     */
    @Parameter(name = "deleteFiles", property = "deleteFiles")
    private String deleteFiles;

    private final MavenSession session;
    private final MavenProject project;
    //private final MojoExecution mojoExecution;

    @Inject
    public ModlistMojo(MavenSession session, MavenProject project) {
        this.session = session;
        this.project = project;
        //this.mojoExecution = mojoExecution;
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
                getLog().warn("Skipped, not a valid JAR or module: " + jarPath);
                return "";
            }
        } catch (RuntimeException e) {
            getLog().error("Skipped because of a problem while reading the file: "+jarPath);
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
        PathMatcher excludeMatcher = null;
        PathMatcher deleteMatcher = null;
        try {
            if (excludeFiles != null && !excludeFiles.isBlank()) {
                excludeMatcher = FileSystems.getDefault().getPathMatcher("glob:"+excludeFiles);
            }
        } catch (Exception e) {
            getLog().warn("Illegal excludeFiles value, no files will be excluded");
        }
        try {
            if (deleteFiles != null && !deleteFiles.isBlank()) {
                deleteMatcher = FileSystems.getDefault().getPathMatcher("glob:"+deleteFiles);
            }
        } catch (Exception e) {
            getLog().warn("Illegal deleteFiles value, no files will be deleted");
        }
        // delete files, then close the first stream to make sure the results have
        // taken effect when generating the list:
        try (var stream = Files.list(inputDirPath)) {
            List<Path> list = stream.toList();
            for (Path path : list) {
                if (deleteMatcher != null && deleteMatcher.matches(path.getFileName())) {
                    try {
                        Files.deleteIfExists(path);
                        getLog().info("Deleted: "+path);
                    } catch (IOException e) {
                        getLog().warn("Unable to delete file: "+path);
                    }
                }
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Unable to read directory "+inputDirPath, e);
        }
        StringBuilder result = new StringBuilder();
        try (var stream = Files.list(inputDirPath)) {
            List<Path> list = stream.toList();
            String name;
            for (Path path : list) {
                if (excludeMatcher!=null && excludeMatcher.matches(path.getFileName())) {
                    getLog().info("Excluded: "+path);
                    continue;
                }
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
