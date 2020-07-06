package com.sri.gradle.internal;

import org.gradle.api.GradleException;

public class RunnerException extends GradleException {
  public RunnerException(String message){
    super(message);
  }

  public RunnerException(String message, Throwable cause){
    super(message, cause);
  }
}
