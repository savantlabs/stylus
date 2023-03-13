package io.savantlabs.stylus.core.http;

import jakarta.annotation.Nullable;

public interface ApiProxy {

  /**
   * Synchronous GET request
   *
   * @param path
   * @param returnType
   * @return
   * @param <R>
   */
  <R> R get(String path, Class<R> returnType);

  /**
   * Synchronous POST request
   *
   * @param path
   * @param body
   * @param returnType
   * @return
   * @param <B>
   * @param <R>
   */
  <B, R> R post(String path, @Nullable B body, Class<R> returnType);

  /**
   * Synchronous PUT request
   *
   * @param path
   * @param body
   * @param returnType
   * @return
   * @param <B>
   * @param <R>
   */
  <B, R> R put(String path, @Nullable B body, Class<R> returnType);

  /**
   * Synchronous DELETE request
   *
   * @param path
   * @param returnType
   * @return
   * @param <R>
   */
  <R> R delete(String path, Class<R> returnType);
}
