package com.sri.gradle.randoop.utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public class MoreFiles {
  private MoreFiles() {
    throw new Error("Cannot be instantiated");
  }

  public static Set<File> getMatchingFiles(Path targetDir, Pattern nameRegExp) {
    Objects.requireNonNull(targetDir);
    Objects.requireNonNull(nameRegExp);

    final List<File> allJavaFiles = Javafinder.findJavaFiles(targetDir);

    return ImmutableStream.setCopyOf(
        allJavaFiles
            .stream()
            .filter(f -> Files.exists(f.toPath()))
            .filter(f -> nameRegExp.asPredicate().test(f.getName())));
  }
}
