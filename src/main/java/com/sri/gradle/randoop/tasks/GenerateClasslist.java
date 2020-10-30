package com.sri.gradle.randoop.tasks;

import static com.sri.gradle.randoop.Constants.BAD_CLASS_LIST_ERROR;

import com.sri.gradle.randoop.Constants;
import com.sri.gradle.randoop.utils.ClasslistGenerator;
import com.sri.gradle.randoop.utils.JavaProjectHelper;
import com.sri.gradle.randoop.utils.Javafinder;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.gradle.api.GradleException;
import org.gradle.api.file.Directory;
import org.gradle.api.tasks.TaskAction;

public class GenerateClasslist extends DescribedTask {

  @TaskAction public void generateClassListFile() {
    final JavaProjectHelper projectHelper = new JavaProjectHelper(getProject());
    final File sourceDir = projectHelper.getSrcMainDir().getAsFile();

    final Path sourceDirPath = sourceDir.toPath();

    if (!Files.exists(sourceDirPath)) throw new GradleException(BAD_CLASS_LIST_ERROR);

    final List<File> javaFiles = Javafinder.findJavaFiles(sourceDirPath, "package-info");

    final Directory resourcesDir = projectHelper.getTestResourcesDir();
    final Path resourcesDirPath = resourcesDir.getAsFile().toPath();

    final Path classListFile = projectHelper.getClassListFile().getAsFile().toPath();
    try {
      ClasslistGenerator.generateClasslist(javaFiles, classListFile, resourcesDirPath);
    } catch (Exception e){
      throw new GradleException(e.getMessage());
    }

    getLogger().quiet("Successfully generated classlist.txt");

  }

  @Override protected String getTaskName() {
    return Constants.TASK_GENERATE_CLASS_LIST;
  }

  @Override protected String getTaskDescription() {
    return Constants.TASK_GENERATE_CLASS_LIST_DESCRIPTION;
  }
}
