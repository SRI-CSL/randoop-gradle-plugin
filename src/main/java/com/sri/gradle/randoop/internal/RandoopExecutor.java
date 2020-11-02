package com.sri.gradle.randoop.internal;

import java.util.Arrays;
import java.util.Objects;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;

public class RandoopExecutor {
  private final Project project;

  public RandoopExecutor(Project project) {
    this.project = project;
  }

  public void exec(Action<RandoopExecSpec> action) {
    Objects.requireNonNull(action);
    final RandoopExecSpec spec = new RandoopExecSpec();
    action.execute(spec);
    exec(spec);
  }

  public void exec(RandoopExecSpec randoopSpec) {
    Objects.requireNonNull(randoopSpec);
    final FileCollection fullClasspath = randoopSpec.getClasspath();
    project.javaexec(
        spec -> {
          spec.setWorkingDir(randoopSpec.getWorkingDir());
          spec.setClasspath(fullClasspath);
          spec.setMain(randoopSpec.getMain());
          spec.setArgs(Arrays.asList(randoopSpec.getArgs()));
          randoopSpec.getConfigureFork().forEach(forkAction -> forkAction.execute(spec));
        });
  }
}
