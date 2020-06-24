package com.sri.gradle.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Immutable collections and maps
 */
public class Immutable {
  private Immutable() {
  }

  /**
   * Creates a new immutable list.
   *
   * @return an immutable list.
   */
  public static <T> List<T> list() {
    return Immutable.listOf(Collections.emptyList());
  }

  public static <T> List<T> listOf(Iterable<? extends T> iterable) {
    return listOf(StreamSupport.stream(iterable.spliterator(), false));
  }

  /**
   * Converts stream of objects into an immutable list.
   *
   * @param stream stream of objects
   * @param <T>    type parameter
   * @return an immutable list.
   */
  public static <T> List<T> listOf(Stream<? extends T> stream) {
    return stream == null ? list() : stream.collect(toImmutableList());
  }

  /**
   * Converts a mutable list into an immutable one.
   *
   * @param list mutable list
   * @param <T>  type parameter
   * @return an immutable list.
   */
  public static <T> List<T> listOf(Collection<? extends T> list) {
    return list == null ? list() : list.stream().collect(toImmutableList());
  }

  /**
   * Creates a new immutable list.
   *
   * @return an immutable list.
   */
  public static <T> Set<T> set() {
    return Immutable.setOf(Collections.emptyList());
  }

  public static <T> Set<T> setOf(Iterable<? extends T> iterable) {
    return setOf(StreamSupport.stream(iterable.spliterator(), false));
  }

  /**
   * Converts a mutable set into an immutable one.
   *
   * @param set mutable set
   * @param <T> type parameter
   * @return an immutable set.
   */
  public static <T> Set<T> setOf(Collection<? extends T> set) {
    return set == null ? set() : set.stream().collect(toImmutableSet());
  }

  /**
   * Converts stream of object into an immutable set.
   *
   * @param stream stream of objects
   * @param <T>    type parameter
   * @return an immutable list.
   */
  public static <T> Set<T> setOf(Stream<? extends T> stream) {
    return stream == null ? set() : stream.collect(toImmutableSet());
  }

  /**
   * Creates an immutable map
   *
   * @return an immutable map
   */
  public static <K, V> Map<K, V> map() {
    return Immutable.mapOf(Collections.emptyMap());
  }

  /**
   * Converts a mutable map into an immutable one.
   *
   * @param map mutable map
   * @param <K> the output type of the key mapping function
   * @param <V> the output type of the value mapping function
   * @return a new immutable map
   */
  public static <K, V> Map<K, V> mapOf(Map<? extends K, ? extends V> map) {
    return mapOf(map.entrySet().stream());
  }

  /**
   * Converts a stream of Map.Entry objects into an immutable map.
   *
   * @param stream stream of Map.Entry object.s
   * @param <K>    the output type of the key mapping function
   * @param <V>    the output type of the value mapping function
   * @return a new immutable map
   */
  public static <K, V> Map<K, V> mapOf(Stream<? extends Map.Entry<? extends K, ? extends V>> stream) {
    return stream.collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  /**
   * Creates a collector that transforms a mutable map into an immutable map.
   *
   * @param keyExtractor   a mapping function to produce keys
   * @param valueExtractor a mapping function to produce values
   * @param <T>            the type of the input elements
   * @param <K>            the output type of the key mapping function
   * @param <V>            the output type of the value mapping function
   * @return a new immutable map
   */
  private static <T, K, V> Collector<T, ?, Map<K, V>> toImmutableMap(
      final Function<? super T, ? extends K> keyExtractor, final Function<? super T, ? extends V> valueExtractor) {

    return Collectors.collectingAndThen(Collectors.toMap(keyExtractor, valueExtractor), Collections::unmodifiableMap);
  }

  /**
   * Creates a collector that transforms a mutable list into an immutable one.
   *
   * @param <T> the type parameter.
   * @return a new collector object.
   */
  private static <T> Collector<T, ?, List<T>> toImmutableList() {
    return Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList);
  }

  /**
   * Creates a collector that transforms a mutable set into an immutable one.
   *
   * @param <T> the type parameter.
   * @return a new collector object.
   */
  private static <T> Collector<T, ?, Set<T>> toImmutableSet() {
    return Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet);
  }
}