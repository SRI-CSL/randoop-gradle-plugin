package com.sri.gradle.utils;

import com.sri.gradle.Constants;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class ClasslistGenerator {
  public static void generateClasslist(List<File> javaFiles, Path classListFile, Path resourcesDirPath){
    final List<String> filenames = Immutable.listOf(javaFiles.stream().map(f -> {
      try {
        final String pathSeparator = System.getProperty("path.separator");
        final String canonicalPath = f.getCanonicalPath();
        final String deletingPrefix = canonicalPath
            .substring(0, f.getCanonicalPath().indexOf(Constants.PATH_TO_SRC_DIR)) + Constants.PATH_TO_SRC_DIR + pathSeparator;

        return canonicalPath.replace(deletingPrefix, "")
            .replaceAll(".java","")
            .replaceAll(pathSeparator,".");

      } catch (IOException ignored){}
      return null;
    }).filter(Objects::nonNull));

    if (!Files.exists(resourcesDirPath)){
      make(resourcesDirPath);
    }

    if (Files.exists(classListFile)){
      deleteFile(classListFile);
    }

    try (BufferedWriter writer = Files.newBufferedWriter(classListFile)){
      for (String filename : filenames) {
        writer.write(filename + "\n");
      }
    } catch (IOException e) {
      throw new RuntimeException("unable to read/write classlist.txt");
    }
  }

  /**
   * Deletes file in path.
   *
   * @param path file path
   */
  public static void deleteFile(Path path){
    try {
      Files.delete(path);
    } catch (IOException ignored){}
  }

  public static void make(Path directory){
    if(directory == null || !directory.toFile().mkdirs()){
      throw new RuntimeException(String.format("Unable to make %s directory", directory));
    }
  }
}
