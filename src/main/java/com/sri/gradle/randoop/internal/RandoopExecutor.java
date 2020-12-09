package com.sri.gradle.randoop.internal;

import com.google.common.collect.ImmutableList;
import com.sri.gradle.randoop.Constants;
import com.sri.gradle.randoop.utils.ImmutableStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;

public class RandoopExecutor {
  private final Project project;
  private OutputStream outputStream;

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
          if (randoopSpec.getOutputStream() != null){
            spec.setStandardOutput(randoopSpec.getOutputStream());
          }
        });

    outputStream = randoopSpec.getOutputStream();
  }

  public void writeOutput(Path path) throws IOException {
    try (Writer writer = Files.newBufferedWriter(path)) {
      for (String line : gatherOutput()){
        writer.write(line);
      }
    }
  }


  public List<String> gatherOutput(){
    if (outputStream == null) return ImmutableList.of();

    return ImmutableStream.listCopyOf(
        Arrays.stream(
            outputStream.toString().split(Constants.NEW_LINE))
            .map(l -> l + "\n")
    );
  }
}
