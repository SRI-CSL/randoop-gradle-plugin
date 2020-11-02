package com.sri.gradle.randoop.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
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

  /**
   * Converts stream of object into an immutable set.
   *
   * @param stream stream of objects
   * @param <T> type parameter
   * @return an immutable list.
   */
  public static <T> Set<T> setCopyOf(Stream<? extends T> stream) {
    return stream == null ? ImmutableSet.of() : stream.collect(ImmutableSet.toImmutableSet());
  }
}
