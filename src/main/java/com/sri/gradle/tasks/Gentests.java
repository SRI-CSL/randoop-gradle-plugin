package com.sri.gradle.tasks;

import com.sri.gradle.Constants;
import com.sri.gradle.internal.RandoopCommand;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

public class Gentests extends RandoopTask {
  private final DirectoryProperty junitOutputDir;
  private final Property<Integer> timeoutSeconds;
  private final RegularFileProperty randoopJar;

  private final Property<Integer> outputLimit;
  private final Property<Boolean> usethreads;
  private final Property<Boolean> noErrorRevealingTests;
  private final Property<Boolean> junitReflectionAllowed;
  private final Property<Boolean> stopOnErrorTest;
  private final Property<String> flakyTestBehavior;
  private final Property<String> junitPackageName;

  private File classListFile;

  @SuppressWarnings("UnstableApiUsage")
  public Gentests() {
    this.junitOutputDir = getProject().getObjects().directoryProperty(); // unchecked warning
    this.timeoutSeconds = getProject().getObjects().property(Integer.class);
    this.randoopJar = getProject().getObjects().fileProperty();  // unchecked warning

    this.outputLimit = getProject().getObjects().property(Integer.class);
    this.usethreads = getProject().getObjects().property(Boolean.class);
    this.noErrorRevealingTests = getProject().getObjects().property(Boolean.class);
    this.junitReflectionAllowed = getProject().getObjects().property(Boolean.class);
    this.stopOnErrorTest = getProject().getObjects().property(Boolean.class);
    this.flakyTestBehavior = getProject().getObjects().property(String.class);
    this.junitPackageName = getProject().getObjects().property(String.class);

    this.classListFile = null;
  }

  @Input public Property<String> getFlakyTestBehavior() {
    return flakyTestBehavior;
  }

  @Input public Property<String> getJunitPackageName() {
    return junitPackageName;
  }

  @OutputDirectory public DirectoryProperty getJunitOutputDir() {
    return this.junitOutputDir;
  }

  @Input @Optional public Property<Integer> getTimeoutSeconds() {
    return timeoutSeconds;
  }

  @InputFile public RegularFileProperty getRandoopJar() {
    return this.randoopJar;
  }

  @Input @Optional public Property<Integer> getOutputLimit() {
    return outputLimit;
  }

  @Input @Optional public Property<Boolean> getUsethreads() {
    return usethreads;
  }

  @Input @Optional public Property<Boolean> getNoErrorRevealingTests() {
    return noErrorRevealingTests;
  }

  @Input @Optional public Property<Boolean> getJunitReflectionAllowed() {
    return junitReflectionAllowed;
  }

  @Input @Optional public Property<Boolean> getStopOnErrorTest() {
    return stopOnErrorTest;
  }

  public void setClassListFile(File file){
    this.classListFile = file;
  }

  public File getClassListFile(){
    return classListFile;
  }

  @TaskAction public void generate() {
    try {
      RandoopCommand command = new RandoopCommand(getTaskName());
      command = command.setClasspath(getProject(), getRandoopJar(), getJunitOutputDir());
      command = command.setTimelimit(getTimeoutSeconds().getOrElse(30));
      command = command.setStopOnErrorTest(getStopOnErrorTest().getOrElse(false));
      command = command.setFlakyTestBehavior(getFlakyTestBehavior().getOrElse("discard"));
      command = command.setNoErrorRevealingTests(getNoErrorRevealingTests().getOrElse(true));
      command = command.setJUnitReflectionAllowed(getJunitReflectionAllowed().getOrElse(false));
      command = command.setJUnitPackageName(getJunitPackageName().get());

      if (getUsethreads().isPresent() && getUsethreads().get()){
        command = command.setUseThreads();
      }

      getLogger().quiet(getClassListFile().toString());
      command = command.setClassListFile(getClassListFile());
      command = command.setOutputLimit(getOutputLimit().getOrElse(2000));
      command = command.setDebugChecks(true);

      getLogger().quiet("About to start generating tests");
      getLogger().quiet("Randoop configuration:");
      Arrays.stream(command.getArgs()).forEach(System.out::println);

      final List<String> out = command.execute();
      out.forEach(System.out::println);

    } catch (Exception e) {
      throw new GradleException("Failed to generate tests", e);
    }

    getLogger().quiet("Successfully generated tests");
  }

  @Override protected String getTaskName() {
    return Constants.TASK_GENERATE_TESTS;
  }

  @Override protected String getTaskDescription() {
    return null;
  }
}