package com.sri.gradle.randoop.extensions;

import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;

public class RandoopPluginExtension {

  private final RegularFileProperty randoopJar;
  private final DirectoryProperty junitOutputDir;
  private final Property<Integer> timeoutSeconds;
  private final Property<Boolean> stopOnErrorTest;
  private final Property<String> flakyTestBehavior;
  private final Property<Boolean> noErrorRevealingTests;
  private final Property<Boolean> junitReflectionAllowed;
  private final Property<Boolean> usethreads;
  private final Property<Integer> outputLimit;
  private final Property<String> junitPackageName;

  @SuppressWarnings("UnstableApiUsage")
  public RandoopPluginExtension(Project project) {
    this.randoopJar = project.getObjects().fileProperty();
    this.junitOutputDir = project.getObjects().directoryProperty();
    this.timeoutSeconds = project.getObjects().property(Integer.class);
    this.stopOnErrorTest = project.getObjects().property(Boolean.class);
    this.flakyTestBehavior = project.getObjects().property(String.class);
    this.noErrorRevealingTests = project.getObjects().property(Boolean.class);
    this.junitReflectionAllowed = project.getObjects().property(Boolean.class);
    this.usethreads = project.getObjects().property(Boolean.class);
    this.outputLimit = project.getObjects().property(Integer.class);
    this.junitPackageName = project.getObjects().property(String.class);
  }

  public Property<String> getFlakyTestBehavior() {
    return flakyTestBehavior;
  }

  public Property<String> getJunitPackageName() {
    return junitPackageName;
  }

  public DirectoryProperty getJunitOutputDir() {
    return junitOutputDir;
  }

  public Property<Integer> getTimeoutSeconds() {
    return timeoutSeconds;
  }

  public RegularFileProperty getRandoopJar() {
    return randoopJar;
  }

  public Property<Integer> getOutputLimit() {
    return outputLimit;
  }

  public Property<Boolean> getUsethreads() {
    return usethreads;
  }

  public Property<Boolean> getNoErrorRevealingTests() {
    return noErrorRevealingTests;
  }

  public Property<Boolean> getJunitReflectionAllowed() {
    return junitReflectionAllowed;
  }

  public Property<Boolean> getStopOnErrorTest() {
    return stopOnErrorTest;
  }
}
