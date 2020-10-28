package com.sri.gradle.randoop.tasks;

import static com.sri.gradle.randoop.Constants.BAD_CLASS_LIST_ERROR;
import static com.sri.gradle.randoop.Constants.PATH_TO_SRC_DIR;

import com.sri.gradle.randoop.utils.ClasslistGenerator;
import com.sri.gradle.randoop.utils.Javafinder;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.Directory;
import org.gradle.api.tasks.TaskAction;

public class GenerateClasslist extends DefaultTask {

  @TaskAction public void generate() {
    final File sourceDir = getProject().getLayout()
        .getProjectDirectory()
        .dir(PATH_TO_SRC_DIR).getAsFile();

    final Path sourceDirPath = sourceDir.toPath();

    if (!Files.exists(sourceDirPath)) throw new GradleException(BAD_CLASS_LIST_ERROR);

    final List<File> javaFiles = Javafinder.findJavaFiles(sourceDirPath, "package-info");

    final Directory resourcesDir = getProject().getLayout()
        .getProjectDirectory()
        .dir("src/test/resources");

    final Path resourcesDirPath = resourcesDir.getAsFile().toPath();

    final Path classListFile = resourcesDir.file("classlist.txt").getAsFile().toPath();
    try {
      ClasslistGenerator.generateClasslist(javaFiles, classListFile, resourcesDirPath);
    } catch (Exception e){
      throw new GradleException(e.getMessage());
    }

    getLogger().quiet("Successfully generated classlist.txt");

  }
}
