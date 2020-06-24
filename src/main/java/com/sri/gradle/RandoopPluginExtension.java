package com.sri.gradle;

import java.io.File;
import org.gradle.api.Project;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

public class RandoopPluginExtension {

  private final Property<String> packageName;
  private final Property<String> outdir;
  private final Property<Integer> timeoutSeconds;
  private final RegularFileProperty randoopJar;

  public RandoopPluginExtension(Project project) {
    this.packageName = project.getObjects().property(String.class);
    this.outdir = project.getObjects().property(String.class);
    this.timeoutSeconds = project.getObjects().property(Integer.class);
    this.randoopJar = project.getObjects().fileProperty();
  }

  public String getPackageName() {
    return packageName.get();
  }

  public Provider<String> getPackageNameProvider() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName.set(packageName);
  }

  public String getOutdir() {
    return this.outdir.get();
  }

  public Provider<String> getOutdirProvider() {
    return outdir;
  }

  public void setOutdir(String outputDir) {
    this.outdir.set(outputDir);
  }

  public int getTimeoutSeconds() {
    return timeoutSeconds.get();
  }

  public Provider<Integer> getTimeoutSecondsProvider() {
    return timeoutSeconds;
  }

  public void setTimeoutSeconds(int timeoutSeconds) {
    this.timeoutSeconds.set(timeoutSeconds);
  }

  public File getRandoopJar() {
    return this.randoopJar.getAsFile().getOrNull();
  }

  public RegularFileProperty getRandoopJarProvider() {
    return randoopJar;
  }

  public void setRandoopJar(File randoopJar) {
    this.randoopJar.set(randoopJar);
  }
}