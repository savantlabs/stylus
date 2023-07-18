package io.savantlabs.stylus.core.http;

import io.savantlabs.stylus.core.util.JsonUtils;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.function.Function;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.ObjectUtils;

@Slf4j
public class ApiProxyImpl implements ApiProxy {

  private final OkHttpClient client;
  private final String baseUri;

  ApiProxyImpl(URI baseUri, Function<Map<String, String>, Map<String, String>> headerModifier) {
    this.baseUri = baseUri.toString();
    this.client = HttpClientFactory.getHttpClient(headerModifier);
  }

  @Override
  public <R> R get(String path, Class<R> returnType) {

    Request request = buildReq(path).get().build();
    return execute(request, returnType);
  }

  @Override
  public <B, R> R post(String path, B body, Class<R> returnType) {
    Request request =
        buildReq(path)
            .post(
                RequestBody.create(
                    ObjectUtils.firstNonNull(JsonUtils.serialize(body), ""),
                    MediaType.get("application/json")))
            .build();
    return execute(request, returnType);
  }

  @Override
  public <B, R> R put(String path, B body, Class<R> returnType) {
    Request request =
        buildReq(path)
            .put(
                RequestBody.create(
                    ObjectUtils.firstNonNull(JsonUtils.serialize(body), ""),
                    MediaType.get("application/json")))
            .build();
    return execute(request, returnType);
  }

  @Override
  public <R> R delete(String path, Class<R> returnType) {
    Request request = buildReq(path).delete().build();
    return execute(request, returnType);
  }

  @SneakyThrows
  private <R> R execute(Request request, Class<R> returnType) {
    Call call = client.newCall(request);
    try (Response response = call.execute()) {
      log.info("{} - {}: {}", call.request().method(), response.code(), call.request().url());
      if (response.isSuccessful()) {
        if (response.body() != null) {
          if (String.class.equals(returnType)) {
            //noinspection unchecked
            return (R) response.body().string();
          } else {
            InputStream is = response.body().byteStream();
            return JsonUtils.deserialize(is, returnType);
          }
        } else {
          return null;
        }
      } else {
        String body =
            response.body() != null ? response.body().string() : String.valueOf(response.code());
        throw ApiProxyException.of(response.code(), body);
      }
    }
  }

  private Request.Builder buildReq(String path) {
    HttpUrl url = HttpUrl.get(getUrl(path));
    return new Request.Builder().url(url);
  }

  private String getUrl(String path) {
    return baseUri + path;
  }
}
