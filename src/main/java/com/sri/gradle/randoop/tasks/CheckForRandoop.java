package com.sri.gradle.randoop.tasks;

import com.sri.gradle.randoop.Constants;
import com.sri.gradle.randoop.internal.RandoopExecutor;
import java.io.File;
import java.nio.charset.StandardCharsets;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.compile.ForkOptions;

public class CheckForRandoop extends DescribedTask {

  @TaskAction
  public void checkForRandoop() {
    try {
      final File randoopJar =
          getProject()
              .getLayout()
              .getProjectDirectory()
              .dir(Constants.PROJECT_LIBS_DIR)
              .file(Constants.RANDOOP_JAR)
              .getAsFile();

      final ForkOptions options = new ForkOptions();

      final RandoopExecutor mainExecutor = new RandoopExecutor(getProject());
      mainExecutor.exec(
          spec -> {
            spec.setClasspath(getProject().files(randoopJar));
            spec.setWorkingDir(getProject().getProjectDir());
            spec.setMain(Constants.RANDOOP_MAIN_CLASS);
            spec.args("help");
            spec.forkOptions(
                fork -> {
                  fork.setWorkingDir(spec.getWorkingDir());
                  fork.setJvmArgs(options.getJvmArgs());
                  fork.setMinHeapSize(options.getMemoryInitialSize());
                  fork.setMaxHeapSize(options.getMemoryMaximumSize());
                  fork.setDefaultCharacterEncoding(StandardCharsets.UTF_8.name());
                });
          });

    } catch (Exception e) {
      throw new GradleException(Constants.BAD_RANDOOP_ERROR);
    }
  }

  @Override
  protected String getTaskName() {
    return Constants.CHECK_FOR_RANDOOP_TASK_NAME;
  }

  @Override
  protected String getTaskDescription() {
    return Constants.CHECK_FOR_RANDOOP_TASK_DESCRIPTION;
  }
}
