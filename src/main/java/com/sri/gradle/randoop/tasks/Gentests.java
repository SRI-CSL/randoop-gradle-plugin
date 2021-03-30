package com.sri.gradle.randoop.tasks;

import com.sri.gradle.randoop.Constants;
import com.sri.gradle.randoop.internal.RandoopExecutor;
import com.sri.gradle.randoop.utils.JavaProjectHelper;
import com.sri.gradle.randoop.utils.MoreFiles;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;

public class Gentests extends PluginExtendedTask {

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

  @Override
  public Property<String> getFlakyTestBehavior() {
    return flakyTestBehavior;
  }

  @Override
  public Property<String> getJunitPackageName() {
    return junitPackageName;
  }

  @Override
  public DirectoryProperty getJunitOutputDir() {
    return this.junitOutputDir;
  }

  @Override
  public Property<Integer> getTimeoutSeconds() {
    return timeoutSeconds;
  }

  @InputFile
  public RegularFileProperty getRandoopJar() {
    return this.randoopJar;
  }

  @Override
  public Property<Integer> getOutputLimit() {
    return outputLimit;
  }

  @Override
  public Property<Boolean> getUsethreads() {
    return usethreads;
  }

  @Override
  public Property<Boolean> getNoErrorRevealingTests() {
    return noErrorRevealingTests;
  }

  @Override
  public Property<Boolean> getJunitReflectionAllowed() {
    return junitReflectionAllowed;
  }

  @Override
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

      Set<File> randoopGeneratedTests =
          MoreFiles.getMatchingFiles(
              junitOutputDir.toPath(),
              Constants.EXPECTED_RANDOOP_TEST_NAME_REGEX);

      if (!randoopGeneratedTests.isEmpty() && projectHelper.hasProperty(Constants.EVIDENCE_ONLY)) {
        getLogger().quiet("Skipped generation of tests. Tests were already generated.");
        return;
      }

      final OutputStream outputStream = new ByteArrayOutputStream();
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
            spec.setOutputStream(outputStream);
          });

      // captures Randoop output information and then
      // writes it to a file, which be picked up by the
      // RandoopDetails task.
      final Path outputLogFile = getProject().getProjectDir().toPath()
          .resolve(Constants.RANDOOP_SUMMARY_FILE_NAME);
      mainExecutor.writeOutput(outputLogFile);
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
