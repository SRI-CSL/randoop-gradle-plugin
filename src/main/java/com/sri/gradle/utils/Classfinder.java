package com.sri.gradle.utils;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class Classfinder {

  private static final char PKG_SEPARATOR = '.';
  private static final char DIR_SEPARATOR = '/';
  private static final String CLASS_FILE_SUFFIX = ".class";
  private static final String BAD_PACKAGE_ERROR = "Unable to get resources from path '%s'. Are you sure the package '%s' exists?";

  public static List<Class<?>> findClasses(final String scannedPackage, final ClassLoader classLoader) {
    final String scannedPath = scannedPackage.replace(PKG_SEPARATOR, DIR_SEPARATOR);
    final URL scannedUrl = classLoader.getResource(scannedPath);

    if (scannedUrl == null) {
      throw new IllegalArgumentException(
          String.format(BAD_PACKAGE_ERROR, scannedPath, scannedPackage));
    }

    final File scannedDir = new File(scannedUrl.getFile());
    return Immutable.listOf(collectClassesIfAny(scannedPackage, classLoader, scannedDir));
  }

  private static List<Class<?>> findClasses(File file, String scannedPackage, ClassLoader classLoader) {
    final List<Class<?>> classes = new ArrayList<>();
    final String resource = scannedPackage + PKG_SEPARATOR + file.getName();

    if (file.isDirectory()) {
      classes.addAll(collectClassesIfAny(resource, classLoader, file));
    } else if (resource.endsWith(CLASS_FILE_SUFFIX)) {
      int endIndex = resource.length() - CLASS_FILE_SUFFIX.length();
      String className = resource.substring(0, endIndex);
      try {
        classes.add(Class.forName(className, false, classLoader));
      } catch (ClassNotFoundException ignore) {
      }
    }

    return classes;
  }

  private static List<Class<?>> collectClassesIfAny(final String scannedPackage, final ClassLoader classLoader,
      final File scannedDir) {

    List<Class<?>> classes = new ArrayList<>();
    final Optional<File[]> optList = Optional.ofNullable(scannedDir.listFiles());

    optList.ifPresent(files -> {
      for (File each : files) {
        classes.addAll(findClasses(each, scannedPackage, classLoader));
      }
    });

    return classes;
  }

}