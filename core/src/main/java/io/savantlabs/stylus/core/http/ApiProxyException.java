package io.savantlabs.stylus.core.http;

import lombok.Getter;

public class ApiProxyException extends RuntimeException {

  @Getter private final int code;

  static ApiProxyException of(int code, String body) {
    if (code >= 500) {
      return new ServerError(code, body);
    } else if (code >= 400) {
      return new ClientError(code, body);
    } else {
      throw new UnsupportedOperationException("Unknown http erroneous status code: " + code);
    }
  }

  private ApiProxyException(int code, String message) {
    super(message);
    this.code = code;
  }

  public static class ServerError extends ApiProxyException {

    private ServerError(int code, String body) {
      super(code, body);
    }
  }

  public static class ClientError extends ApiProxyException {

    private ClientError(int code, String body) {
      super(code, body);
    }
  }
}
