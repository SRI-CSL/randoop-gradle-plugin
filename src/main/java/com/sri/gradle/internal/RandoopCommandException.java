package com.sri.gradle.internal;

import org.gradle.api.GradleException;

public class RandoopCommandException extends GradleException {
  public RandoopCommandException(String message){
    super(message);
  }

  public RandoopCommandException(String message, Throwable cause){
    super(message, cause);
  }
}
