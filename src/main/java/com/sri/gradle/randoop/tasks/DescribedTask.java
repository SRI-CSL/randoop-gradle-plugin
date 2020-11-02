package com.sri.gradle.randoop.tasks;

import javax.annotation.Nonnull;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Internal;

public abstract class DescribedTask extends DefaultTask {
  @Internal
  protected abstract String getTaskName();

  @Internal
  protected abstract String getTaskDescription();

  @Override @Nonnull public String toString() {
    return getTaskName() + ": " + getTaskDescription();
  }
}
