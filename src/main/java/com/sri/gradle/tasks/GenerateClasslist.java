package com.sri.gradle.tasks;

import com.sri.gradle.Constants;
import com.sri.gradle.utils.Immutable;
import com.sri.gradle.utils.Javafinder;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.Directory;
import org.gradle.api.tasks.TaskAction;

public class GenerateClasslist extends DefaultTask {
  private static final String BAD_CLASS_LIST_ERROR = "Unable to find src directory. Are you sure src exists?";

  @TaskAction public void generate() {
    final File sourceDir = getProject().getLayout()
        .getProjectDirectory()
        .dir(Constants.PATH_TO_SRC_DIR).getAsFile();

    final Path sourceDirPath = sourceDir.toPath();

    if (!Files.exists(sourceDirPath)) throw new GradleException(BAD_CLASS_LIST_ERROR);

    final List<File> javaFiles = Javafinder.findJavaFiles(sourceDirPath, "package-info");

    final List<String> filenames = Immutable.listOf(javaFiles.stream().map(f -> {
      try {
        final String canonicalPath = f.getCanonicalPath();
        final String deletingPrefix = canonicalPath
            .substring(0, f.getCanonicalPath().indexOf(Constants.PATH_TO_SRC_DIR)) + Constants.PATH_TO_SRC_DIR + "/";

        return canonicalPath.replace(deletingPrefix, "")
            .replaceAll(".java","")
            .replaceAll("/",".");
      } catch (IOException ignored){}
      return null;
    }).filter(Objects::nonNull));

    final Directory resourcesDir = getProject().getLayout()
        .getProjectDirectory()
        .dir("src/test/resources");

    final Path resourcesDirPath = resourcesDir.getAsFile().toPath();

    if (!Files.exists(resourcesDirPath)){
      make(resourcesDirPath);
    }

    final Path classListFile = resourcesDir.file("classlist.txt").getAsFile().toPath();

    if (Files.exists(classListFile)){
      deleteFile(classListFile);
    }

    try (BufferedWriter writer = Files.newBufferedWriter(classListFile)){
      for (String filename : filenames) {
        writer.write(filename + "\n");
      }
    } catch (IOException e) {
      throw new GradleException("unable to read/write classlist.txt");
    }

    getLogger().quiet("Successfully generated classlist.txt");

  }

  /**
   * Deletes file in path.
   *
   * @param path file path
   */
  private static void deleteFile(Path path){
    try {
      Files.delete(path);
    } catch (IOException ignored){}
  }

  private static void make(Path directory){
    if(directory == null || !directory.toFile().mkdirs()){
      throw new GradleException(String.format("Unable to make %s directory", directory));
    }
  }
}
