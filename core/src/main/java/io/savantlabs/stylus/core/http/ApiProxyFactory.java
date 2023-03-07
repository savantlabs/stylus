package io.savantlabs.stylus.core.http;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.MapUtils;

@UtilityClass
public class ApiProxyFactory {

  private static final String AUTHORIZATION = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";

  public static ApiProxy buildProxy(URI baseUri) {
    return new ApiProxyImpl(baseUri, Function.identity());
  }

  public static ApiProxy buildProxy(URI baseUri, BearerTokenSupplier tokenSupplier) {
    return new ApiProxyImpl(
        baseUri,
        headers -> {
          Map<String, String> headers2 = new HashMap<>();
          if (MapUtils.isNotEmpty(headers)) {
            headers.forEach(
                (k, v) -> {
                  if (!AUTHORIZATION.equalsIgnoreCase(k)) {
                    headers2.put(k, v);
                  }
                });
          }
          String token = tokenSupplier.getToken();
          if (!token.startsWith(BEARER_PREFIX)) {
            token = BEARER_PREFIX + token;
          }
          headers2.put(AUTHORIZATION, token);
          return headers2;
        });
  }
}
