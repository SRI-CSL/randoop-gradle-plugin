package com.sri.gradle.randoop.internal;

import com.google.common.base.Preconditions;
import java.util.NoSuchElementException;

public enum OutOfMemoryError {
  DISCARD_FROM_TESTS("INVALID"),
  INCLUDE_IN_ERROR_REVEALING_TESTS("ERROR"),
  INCLUDE_IN_REGRESSION_TESTS("EXPECTED");

  private final String value;
  OutOfMemoryError(String value){
    this.value = value;
  }

  public String getValue(){
    return value;
  }

  public static OutOfMemoryError of(String value){
    Preconditions.checkArgument(value != null && !value.isEmpty());

    for (OutOfMemoryError each : values()){
      if (each.getValue().equals(value)) return each;
    }

    throw new NoSuchElementException("OutOfMemoryError value not found");
  }


  @Override public String toString() {
    return getValue();
  }
}
