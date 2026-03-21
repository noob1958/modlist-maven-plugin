# The `modlist-maven-plugin`

This plugin is meant to be useful as a bridge between directory contents, on the one hand, and maven
properties, on the other. 

As part of your build process, you may at times want to copy modular
JAR files from various locations into a directory (e.g. with the `maven-dependency-plugin`), and then 
hand these collected modules to a plugin or (using the `maven-antrun-plugin`) a java command line tool. This is unproblematic if the tool's parameter accepts a directory. Unfortunately, some require a 
comma-separated list of module names instead; an example is parameter `--add-modules` of `jlink`.

Here, the `modlist-maven-plugin` can help. It **scans a specifiable directory and writes
the fully qualified names of all contained modules into a new maven session property**. The default name of the property is "modlist". You can use this property to hand the module name list to other plugins and tools, with `${modlist}`.

## Usage

Add the following to the plugins section of your project's POM file:

```XML
<plugin>
    <groupId>com.github.noob1958</groupId>
    <artifactId>modlist-maven-plugin</artifactId>
    <version>1.0</version>
    <executions>
        <execution>
            <!-- bind the modlist:list goal to a lifecycle phase of your choice: -->
            <phase>integration-test</phase>
            <goals><goal>list</goal></goals>
            <configuration>
                <inputDir>libs</inputDir>
                <outputPropertyName>my.property.name</outputPropertyName>
                <deleteFiles>javafx-*.jar</deleteFiles>
                <excludeFiles>*-mac.jar</deleteFiles>
            </configuration>
        </execution>
    </executions>
</plugin>
```

This configuration would find every module in folder `libs` and its subfolders.

The plugin has two goals (`list` and `help`) and these configuration tags: 
* inputDir: the path to the folder to read, relative to the project's base directory. This tag is mandatory.
* outputPropertyName: the name of the session property that will receive the output. Optional; the default is "modlist".
* excludeFiles: An optional glob-format string indicating which of the files in the inputDir to exclude before generating the module list. E.g. "abc-*-def.jar" will exclude "abc-ghi-def.jar".
* deleteFiles: An optional glob-format string indicating which of the files in the inputDir to delete before generating the module list. E.g. "abc-*-def.jar" will delete "abc-ghi-def.jar".

## Prerequisites

* written in Java 21
* compiled with Oracle OpenJDK 25.0.2
* requires Maven 3.9.14+
