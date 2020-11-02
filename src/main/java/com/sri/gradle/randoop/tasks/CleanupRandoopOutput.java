package com.sri.gradle.randoop.tasks;

import com.sri.gradle.randoop.Constants;
import com.sri.gradle.randoop.utils.ImmutableStream;
import com.sri.gradle.randoop.utils.JavaProjectHelper;
import com.sri.gradle.randoop.utils.MoreFiles;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Set;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

@SuppressWarnings("UnstableApiUsage")
public class CleanupRandoopOutput extends DescribedTask {
  private final DirectoryProperty junitOutputDir;

  public CleanupRandoopOutput(){
    this.junitOutputDir = getProject().getObjects().directoryProperty(); // unchecked warning
  }

  @TaskAction
  public void cleanupRandoopOutput(){
    Set<File> randoopGeneratedTests = MoreFiles.getMatchingFiles(
        getJunitOutputDir().getAsFile().get().toPath(), Constants.EXPECTED_RANDOOP_TEST_NAME_REGEX);

    final JavaProjectHelper helper = new JavaProjectHelper(getProject());
    final File classListFile = helper.getClassListFile().getAsFile();
    deleteFile(classListFile);

    if (randoopGeneratedTests.isEmpty()) {
      getLogger().quiet("There's nothing from Randoop to delete.");
      return;
    }

    randoopGeneratedTests.forEach(CleanupRandoopOutput::deleteFile);

    randoopGeneratedTests = ImmutableStream.setCopyOf(randoopGeneratedTests.stream().filter(f -> Files.exists(f.toPath())));

    if (!randoopGeneratedTests.isEmpty()){
      throw new GradleException("Unable to delete all Randoop generated files");
    }

    getLogger().quiet("Cleaned up all Randoop-generated files");
  }

  /**
   * Deletes a file.
   *
   * @param file file object
   */
  private static void deleteFile(File file){
    Objects.requireNonNull(file);
    try {
      Files.delete(file.toPath());
    } catch (IOException ignored){}
  }

  @OutputDirectory public DirectoryProperty getJunitOutputDir() {
    return this.junitOutputDir;
  }

  @Override protected String getTaskName() {
    return Constants.CLEANUP_RANDOOP_TASK_NAME;
  }

  @Override protected String getTaskDescription() {
    return Constants.CLEANUP_RANDOOP_TASK_DESCRIPTION;
  }
}
