package com.sri.gradle;

import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;

public class RandoopPluginExtension {

  private final Property<String> packageName;
  private final Property<Integer> timeoutSeconds;
  private final DirectoryProperty junitOutputDir;
  private final RegularFileProperty randoopJar;


  private final Property<Integer> outputLimit;
  private final Property<Boolean> usethreads;
  private final Property<Boolean> noErrorRevealingTests;
  private final Property<Boolean> junitReflectionAllowed;
  private final Property<Boolean> stopOnErrorTest;
  private final Property<String> flakyTestBehavior;


  @SuppressWarnings("UnstableApiUsage")
  public RandoopPluginExtension(Project project) {
    this.packageName = project.getObjects().property(String.class);
    this.timeoutSeconds = project.getObjects().property(Integer.class);
    this.junitOutputDir = project.getObjects().directoryProperty();
    this.randoopJar = project.getObjects().fileProperty();

    this.outputLimit = project.getObjects().property(Integer.class);
    this.usethreads = project.getObjects().property(Boolean.class);
    this.noErrorRevealingTests = project.getObjects().property(Boolean.class);
    this.junitReflectionAllowed = project.getObjects().property(Boolean.class);
    this.stopOnErrorTest = project.getObjects().property(Boolean.class);
    this.flakyTestBehavior = project.getObjects().property(String.class);
  }

  public Property<String> getFlakyTestBehavior() {
    return flakyTestBehavior;
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