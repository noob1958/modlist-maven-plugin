package com.github.noob1958;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Goal which reads the names of all modules in a folder into a maven property.
 */
@Mojo(name = "list")
public class ModlistMojo extends AbstractMojo {
    /**
     * Location of the folder.
     */
    @Parameter(name = "inputDir", property = "inputDir", required = false)
    private String inputDir;

    @Override
    public void execute() throws MojoExecutionException {
        if (inputDir==null || inputDir.isBlank()) return;
        Path inputDirPath = Path.of(inputDir);
        //TODO resolve against project.getBasedir()!
        if (!Files.isDirectory(inputDirPath)) throw new MojoExecutionException("Directory not found: "+inputDir);
        getLog().info("Reading directory");
//        try (...) {
//
//        } catch (IOException e) {
//            throw new MojoExecutionException("Error ...", e);
//        }
        getLog().info("Stored the following string in property ...: ...");
    }

}
