package com.sri.gradle.randoop.internal;

import org.gradle.api.GradleException;

public class RandoopExecException extends GradleException {
  public RandoopExecException(String message){
    super(message);
  }

  public RandoopExecException(String message, Throwable cause){
    super(message, cause);
  }
}
