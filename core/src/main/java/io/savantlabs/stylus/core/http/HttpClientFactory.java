package io.savantlabs.stylus.core.http;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import lombok.experimental.UtilityClass;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

@UtilityClass
public class HttpClientFactory {

  static OkHttpClient getHttpClient(
      Function<Map<String, String>, Map<String, String>> headerModifier) {
    OkHttpClient.Builder builder = new OkHttpClient.Builder();
    ConnectionPool okHttpConnectionPool = new ConnectionPool(64, 10, TimeUnit.MINUTES);
    builder.connectionPool(okHttpConnectionPool);
    builder.readTimeout(1, TimeUnit.MINUTES);
    builder.connectTimeout(10, TimeUnit.SECONDS);
    builder.retryOnConnectionFailure(true);
    builder.addInterceptor(new HeaderInterceptor(headerModifier));
    return builder.build();
  }
}
