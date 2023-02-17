package io.savantlabs.stylus.core.http;

import com.fasterxml.jackson.databind.JsonNode;
import io.savantlabs.stylus.core.http.ApiProxyException.ClientError;
import io.savantlabs.stylus.core.http.ApiProxyException.ServerError;
import java.net.URI;
import java.util.Map;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class ApiProxyTests {

  @Test
  @SneakyThrows
  void testTokenProvider() {
    try (TestHttpServer server = new TestHttpServer()) {
      int port = server.getPort();

      ApiProxy proxy1 = ApiProxyFactory.buildProxy(URI.create("http://localhost:" + port));
      Assertions.assertThatThrownBy(() -> proxy1.get("/200", JsonNode.class))
          .isInstanceOf(ClientError.class)
          .satisfies(t -> Assertions.assertThat(((ClientError) t).getCode()).isEqualTo(401));

      ApiProxy proxy2 =
          ApiProxyFactory.buildProxy(
              URI.create("http://localhost:" + port), () -> TestHttpServer.TOKEN);
      JsonNode resp = proxy2.get("/200", JsonNode.class);
      Assertions.assertThat(resp.get("status").asText("")).isEqualTo("Ok!");
    }
  }

  @Test
  @SneakyThrows
  void testApiProxy200() {
    try (TestHttpServer server = new TestHttpServer()) {
      int port = server.getPort();
      ApiProxy proxy =
          ApiProxyFactory.buildProxy(
              URI.create("http://localhost:" + port), () -> TestHttpServer.TOKEN);

      JsonNode resp = proxy.get("/200", JsonNode.class);
      Assertions.assertThat(resp.get("status").asText("")).isEqualTo("Ok!");

      resp = proxy.post("/200", null, JsonNode.class);
      Assertions.assertThat(resp.get("status").asText("")).isEqualTo("Ok!");
      resp = proxy.post("/200", Map.of("key", "value"), JsonNode.class);
      Assertions.assertThat(resp.get("status").asText("")).isEqualTo("Ok!");

      resp = proxy.put("/200", null, JsonNode.class);
      Assertions.assertThat(resp.get("status").asText("")).isEqualTo("Ok!");
      resp = proxy.put("/200", Map.of("key", "value"), JsonNode.class);
      Assertions.assertThat(resp.get("status").asText("")).isEqualTo("Ok!");

      resp = proxy.delete("/200", JsonNode.class);
      Assertions.assertThat(resp.get("status").asText("")).isEqualTo("Ok!");
    }
  }

  @Test
  @SneakyThrows
  void testApiProxy4XX() {
    try (TestHttpServer server = new TestHttpServer()) {
      int port = server.getPort();
      Assertions.assertThat(port).isGreaterThan(0);
      final ApiProxy proxy =
          ApiProxyFactory.buildProxy(
              URI.create("http://localhost:" + port), () -> TestHttpServer.TOKEN);

      Assertions.assertThatThrownBy(() -> proxy.get("/no-such-path", JsonNode.class))
          .isInstanceOf(ClientError.class)
          .satisfies(t -> Assertions.assertThat(((ClientError) t).getCode()).isEqualTo(404));

      Assertions.assertThatThrownBy(() -> proxy.post("/401", Map.of(), JsonNode.class))
          .isInstanceOf(ClientError.class)
          .satisfies(t -> Assertions.assertThat(((ClientError) t).getCode()).isEqualTo(401));
      Assertions.assertThatThrownBy(() -> proxy.put("/403", Map.of(), JsonNode.class))
          .isInstanceOf(ClientError.class)
          .satisfies(t -> Assertions.assertThat(((ClientError) t).getCode()).isEqualTo(403));
    }
  }

  @Test
  @SneakyThrows
  void testApiProxy5XX() {
    try (TestHttpServer server = new TestHttpServer()) {
      int port = server.getPort();
      Assertions.assertThat(port).isGreaterThan(0);
      final ApiProxy proxy =
          ApiProxyFactory.buildProxy(
              URI.create("http://localhost:" + port), () -> TestHttpServer.TOKEN);

      Assertions.assertThatThrownBy(() -> proxy.get("/502", JsonNode.class))
          .isInstanceOf(ServerError.class)
          .satisfies(t -> Assertions.assertThat(((ServerError) t).getCode()).isEqualTo(502));
      Assertions.assertThatThrownBy(() -> proxy.post("/503", null, JsonNode.class))
          .isInstanceOf(ServerError.class)
          .satisfies(t -> Assertions.assertThat(((ServerError) t).getCode()).isEqualTo(503));
    }
  }

  @Test
  @SneakyThrows
  void testApiProxy500() {
    try (TestHttpServer server = new TestHttpServer()) {
      int port = server.getPort();
      Assertions.assertThat(port).isGreaterThan(0);
      final ApiProxy proxy =
          ApiProxyFactory.buildProxy(
              URI.create("http://localhost:" + port), () -> TestHttpServer.TOKEN);
      Assertions.assertThatThrownBy(() -> proxy.get("/500", JsonNode.class))
          .isInstanceOf(ServerError.class)
          .satisfies(t -> Assertions.assertThat(((ServerError) t).getCode()).isEqualTo(500));
      ;
    }
  }
}
