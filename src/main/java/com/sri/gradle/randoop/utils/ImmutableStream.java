package com.sri.gradle.randoop.utils;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.stream.Stream;

public class ImmutableStream {
  private ImmutableStream(){
    throw new Error("Cannot be instantiated");
  }

  /**
   * Converts stream of objects into an immutable list.
   *
   * @param stream stream of objects
   * @param <T> type parameter
   * @return an immutable list.
   */
  public static <T> List<T> listCopyOf(Stream<? extends T> stream) {
    return stream == null ? ImmutableList.of() : stream.collect(ImmutableList.toImmutableList());
  }
}
