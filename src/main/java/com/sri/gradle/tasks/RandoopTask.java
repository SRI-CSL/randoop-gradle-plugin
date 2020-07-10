package com.sri.gradle.tasks;

import javax.annotation.Nonnull;
import org.gradle.api.DefaultTask;

public abstract class RandoopTask extends DefaultTask {
  protected abstract String getTaskName();

  @Override @Nonnull public String toString() {
    return getTaskName();
  }
}
