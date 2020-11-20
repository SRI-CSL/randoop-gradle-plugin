package com.sri.gradle.randoop.tasks;

import com.sri.gradle.randoop.Constants;
import com.sri.gradle.randoop.internal.RandoopExecutor;
import com.sri.gradle.randoop.utils.JavaProjectHelper;
import java.io.File;
import java.util.Objects;
import java.util.Set;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

public class Gentests extends DescribedTask {
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
  public Gentests() {
    this.junitOutputDir = getProject().getObjects().directoryProperty(); // unchecked warning
    this.timeoutSeconds = getProject().getObjects().property(Integer.class);
    this.randoopJar = getProject().getObjects().fileProperty(); // unchecked warning

    this.outputLimit = getProject().getObjects().property(Integer.class);
    this.usethreads = getProject().getObjects().property(Boolean.class);
    this.noErrorRevealingTests = getProject().getObjects().property(Boolean.class);
    this.junitReflectionAllowed = getProject().getObjects().property(Boolean.class);
    this.stopOnErrorTest = getProject().getObjects().property(Boolean.class);
    this.flakyTestBehavior = getProject().getObjects().property(String.class);
    this.junitPackageName = getProject().getObjects().property(String.class);
  }

  @Input
  public Property<String> getFlakyTestBehavior() {
    return flakyTestBehavior;
  }

  @Input
  public Property<String> getJunitPackageName() {
    return junitPackageName;
  }

  @OutputDirectory
  public DirectoryProperty getJunitOutputDir() {
    return this.junitOutputDir;
  }

  @Input
  @Optional
  public Property<Integer> getTimeoutSeconds() {
    return timeoutSeconds;
  }

  @InputFile
  public RegularFileProperty getRandoopJar() {
    return this.randoopJar;
  }

  @Input
  @Optional
  public Property<Integer> getOutputLimit() {
    return outputLimit;
  }

  @Input
  @Optional
  public Property<Boolean> getUsethreads() {
    return usethreads;
  }

  @Input
  @Optional
  public Property<Boolean> getNoErrorRevealingTests() {
    return noErrorRevealingTests;
  }

  @Input
  @Optional
  public Property<Boolean> getJunitReflectionAllowed() {
    return junitReflectionAllowed;
  }

  @Input
  @Optional
  public Property<Boolean> getStopOnErrorTest() {
    return stopOnErrorTest;
  }

  @TaskAction
  public void generateTests() {
    try {
      final JavaProjectHelper projectHelper = new JavaProjectHelper(getProject());
      final File classListFile =
          projectHelper.findClassListFile().orElseThrow(IllegalArgumentException::new);

      final File randoopJar = Objects.requireNonNull(getRandoopJar().getAsFile().get());
      final Set<File> classpath = projectHelper.getClasspath(randoopJar);

      final File junitOutputDir = getJunitOutputDir().getAsFile().get();

      final RandoopExecutor mainExecutor = new RandoopExecutor(getProject());
      mainExecutor.exec(
          spec -> {
            spec.setWorkingDir(getProject().getProjectDir());
            spec.setClasspath(getProject().files(classpath));
            spec.setMain(Constants.RANDOOP_MAIN_CLASS);
            spec.setCommand("gentests");
            spec.setTimelimit(getTimeoutSeconds().getOrElse(30));
            spec.setStopOnErrorTest(getStopOnErrorTest().getOrElse(false));
            spec.setFlakyTestBehavior(getFlakyTestBehavior().getOrElse("discard"));
            spec.setNoErrorRevealingTests(getNoErrorRevealingTests().getOrElse(true));
            spec.setJUnitReflectionAllowed(getJunitReflectionAllowed().getOrElse(false));
            spec.setJUnitPackageName(getJunitPackageName().get());
            spec.setJUnitOutputDir(getProject().getProjectDir(), junitOutputDir);

            if (getUsethreads().isPresent() && getUsethreads().get()) {
              spec.setUseThreads();
            }

            spec.setClassListFile(getProject().getProjectDir(), classListFile);
            spec.setOutputLimit(getOutputLimit().getOrElse(2000));
            spec.setDebugChecks(true);
            spec.setRandoopLog(getProject().getProjectDir().toPath(), "randoop-log.txt");
          });

    } catch (Exception e) {
      throw new GradleException("Failed to generate tests", e);
    }

    getLogger().quiet("Successfully generated tests");
  }

  @Override
  protected String getTaskName() {
    return Constants.GENERATE_TESTS_TASK_NAME;
  }

  @Override
  protected String getTaskDescription() {
    return Constants.GENERATE_TESTS_TASK_DESCRIPTION;
  }
}
