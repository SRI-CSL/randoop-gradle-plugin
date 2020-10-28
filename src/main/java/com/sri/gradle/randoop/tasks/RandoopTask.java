package com.sri.gradle.randoop.tasks;

import javax.annotation.Nonnull;
import org.gradle.api.DefaultTask;

public abstract class RandoopTask extends DefaultTask {
  protected abstract String getTaskName();
  protected abstract String getTaskDescription();

  @Override @Nonnull public String toString() {
    return getTaskName();
  }
}