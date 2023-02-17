package io.savantlabs.stylus.core.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

@Slf4j
public class HeaderInterceptor implements Interceptor {

  public static final Map<String, String> DEFAULT_HEADERS = getDefaultHeaders();

  private static Map<String, String> getDefaultHeaders() {
    Map<String, String> headerMap = new HashMap<>();
    headerMap.put("Accept", "application/json");
    headerMap.put("Content-Type", "application/json");
    return headerMap;
  }

  private final Map<String, String> headerMap;
  private final Function<Map<String, String>, Map<String, String>> modifier;

  public HeaderInterceptor(Function<Map<String, String>, Map<String, String>> modifier) {
    this(DEFAULT_HEADERS, modifier);
  }

  public HeaderInterceptor(
      Map<String, String> headerMap, Function<Map<String, String>, Map<String, String>> modifier) {
    this.headerMap = Map.copyOf(headerMap);
    this.modifier = modifier;
  }

  @Override
  public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();
    Request newRequest;
    try {
      Request.Builder builder = request.newBuilder();
      modifier.apply(headerMap).forEach(builder::addHeader);
      newRequest = builder.build();
    } catch (Exception e) {
      log.error("failed to inject headers", e);
      return chain.proceed(request);
    }
    return chain.proceed(newRequest);
  }
}
