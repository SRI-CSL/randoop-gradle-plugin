package com.sri.gradle.randoop.tasks;

import com.sri.gradle.randoop.Constants;
import com.sri.gradle.randoop.utils.ImmutableStream;
import com.sri.gradle.randoop.utils.MoreFiles;
import java.io.File;
import java.util.Objects;
import java.util.Set;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

@SuppressWarnings("UnstableApiUsage")
public class CheckForRandoopTests extends DescribedTask {
  private final DirectoryProperty junitOutputDir;

  public CheckForRandoopTests() {
    this.junitOutputDir = getProject().getObjects().directoryProperty(); // unchecked warning
  }

  @TaskAction
  public void checkForRandoopTests() {
    final Set<File> outputDirs =
        ImmutableStream.setCopyOf(
            MoreFiles.getMatchingFiles(
                    getJunitOutputDir().getAsFile().get().toPath(),
                    Constants.EXPECTED_RANDOOP_TEST_NAME_REGEX)
                .stream()
                .filter(Objects::nonNull)
                .map(File::getParentFile));

    if (outputDirs.isEmpty()) {
      throw new GradleException("Unable to find Randoop-generated test cases.");
    }

    getLogger().quiet("Found Randoop-generated files at " + getJunitOutputDir().get().getAsFile());
  }

  @OutputDirectory
  public DirectoryProperty getJunitOutputDir() {
    return this.junitOutputDir;
  }

  @Override
  protected String getTaskName() {
    return Constants.CHECK_FOR_RANDOOP_TESTS_TASK_NAME;
  }

  @Override
  protected String getTaskDescription() {
    return Constants.CHECK_FOR_RANDOOP_TESTS_TASK_DESCRIPTION;
  }
}
