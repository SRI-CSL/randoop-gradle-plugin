package com.sri.gradle.internal;

import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;

/**
 * The generator descriptor for user-provided information.
 */
public class GeneratorOptions {

  private final String outputDir;
  private final String packageName;
  private final int timeoutSeconds;
  private final String randoopJarPath;

  public GeneratorOptions(String randoopJarPath, String outputDir, String packageName, int timeoutSeconds) {
    this.randoopJarPath = randoopJarPath;
    this.outputDir = outputDir;
    this.packageName = packageName;
    this.timeoutSeconds = timeoutSeconds;
  }

  public String getRandoopJarPath() {
    return randoopJarPath;
  }

  public String getOutputDir() {
    return outputDir;
  }

  public String getPackageName() {
    return packageName;
  }

  public int getTimeoutSeconds() {
    return timeoutSeconds;
  }

}
