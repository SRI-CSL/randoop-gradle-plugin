package com.sri.gradle.tasks;

import com.sri.gradle.internal.RandoopRunner;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

public class Gentests extends DefaultTask {

  private static final String BAD_RANDOOP_ERROR = "Unable to run Randoop. Are you sure randoop.jar is in your path?";

  private final Property<String> packageName;
  private final DirectoryProperty outdir;
  private final Property<Integer> timeoutSeconds;
  private final RegularFileProperty randoopJar;

  public Gentests() {
    this.packageName = getProject().getObjects().property(String.class);
    this.outdir = getProject().getObjects().directoryProperty();
    this.timeoutSeconds = getProject().getObjects().property(Integer.class);
    this.randoopJar = getProject().getObjects().fileProperty();
  }


  public void setPackageName(String packageName) {
    this.packageName.set(packageName);
  }

  public void setPackageName(Provider<String> packageName) {
    this.packageName.set(packageName);
  }

  @Input public String getPackageName() {
    return packageName.get();
  }

  @OutputDirectory public DirectoryProperty getOutdir() {
    return this.outdir;
  }

  public void setTimeoutSeconds(Integer timeoutSeconds) {
    this.timeoutSeconds.set(timeoutSeconds);
  }

  public void setTimeoutSeconds(Provider<Integer> timeoutSeconds) {
    this.timeoutSeconds.set(timeoutSeconds);
  }

  @Input @Optional public Integer getTimeoutSeconds() {
    return timeoutSeconds.getOrElse(30);
  }

  @InputFile public RegularFileProperty getRandoopJar() {
    return this.randoopJar;
  }

  @TaskAction public void generate() {
    try {
      final RandoopRunner runner = new RandoopRunner(getProject())
          .setClasspath(getRandoopJar(), getOutdir())
          .setTimelimit(getTimeoutSeconds())
          .setPackageName(getPackageName());

      getLogger().quiet("About to start generating tests");
      if (!runner.run()){
        throw new GradleException(BAD_RANDOOP_ERROR);
      }
    } catch (Exception e) {
      throw new GradleException("Failed to generate tests", e);
    }

    getLogger().quiet("Successfully generated tests");
  }
}