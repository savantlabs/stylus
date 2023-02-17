package io.savantlabs.stylus.core.http;

public interface BearerTokenSupplier {

  /**
   * Generate a `token` to be added to the `Authorization` header after prefixing with `Bearer `
   *
   * @return
   */
  String getToken();
}
