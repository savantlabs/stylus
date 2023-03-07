package io.savantlabs.stylus.core.http;

import com.fasterxml.jackson.databind.JsonNode;
import io.savantlabs.stylus.core.util.JsonUtils;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okio.ByteString;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class WsClientTests {
  private static final Random RANDOM = new Random(System.currentTimeMillis());
  private static final int N = 10;

  @Test
  @SneakyThrows
  void testWebsocket() {
    ExecutorService tp = Executors.newFixedThreadPool(3);
    try (TestHttpServer server = new TestHttpServer()) {
      int port = server.getPort();
      URI serverUri = URI.create(String.format("http://localhost:%d/ws", port));
      try (TestClient client1 = runTestClient("client-1", serverUri, tp)) {
        try (TestClient client2 = runTestClient("client-2", serverUri, tp)) {
          try (TestClient client3 = runTestClient("client-3", serverUri, tp)) {
            client1.future.get(1, TimeUnit.MINUTES);
            client2.future.get(1, TimeUnit.MINUTES);
            client3.future.get(1, TimeUnit.MINUTES);

            verifyTestClient(client1);
            verifyTestClient(client2);
            verifyTestClient(client3);
          }
        }
      }
      Assertions.assertThat(server.jsonMessages).hasSize(N * 3);
      Assertions.assertThat(server.binaryMessages).hasSize(N * 3);
    }
  }

  private TestClient runTestClient(String clientName, URI serverUri, ExecutorService tp) {
    final TestClient client = new TestClient(clientName, serverUri);
    client.future =
        tp.submit(
            () -> {
              for (int i = 0; i < N; i++) {
                Map<String, Object> payload = Map.of("from", clientName, "data", i);
                ByteString bytes = ByteString.encodeUtf8(JsonUtils.serialize(payload));
                client.client.sendJson(payload);
                try {
                  Thread.sleep(RANDOM.nextInt(250));
                } catch (InterruptedException e) {
                  throw new RuntimeException(e);
                }
                client.client.sendBytes(bytes);
                try {
                  Thread.sleep(RANDOM.nextInt(250));
                } catch (InterruptedException e) {
                  throw new RuntimeException(e);
                }
              }
              if (client.textQ.size() < N) {
                synchronized (client.textQ) {
                  try {
                    client.textQ.wait(5000);
                  } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                  }
                }
              }
              if (client.binaryQ.size() < N) {
                synchronized (client.binaryQ) {
                  try {
                    client.binaryQ.wait(5000);
                  } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                  }
                }
              }
            });
    return client;
  }

  private void verifyTestClient(TestClient client) {
    Assertions.assertThat(client.textQ).hasSize(N);
    Assertions.assertThat(client.binaryQ).hasSize(N);
    client.textQ.forEach(
        t -> {
          JsonNode json = JsonUtils.deserialize(t, JsonNode.class);
          Assertions.assertThat(json.get("from").asText()).isEqualTo(client.name);
        });
    client.binaryQ.forEach(
        b -> {
          String str = b.string(StandardCharsets.UTF_8);
          JsonNode json = JsonUtils.deserialize(str, JsonNode.class);
          Assertions.assertThat(json.get("from").asText()).isEqualTo(client.name);
        });
  }

  private static class TestClient implements AutoCloseable {

    private final String name;
    private final Queue<String> textQ = new LinkedList<>();
    private final Queue<ByteString> binaryQ = new LinkedList<>();
    private final WsClient client;

    private Future<?> future;

    TestClient(String name, URI serverUri) {
      this.name = name;
      WsListener listener =
          new WsListener() {
            @Override
            public void onText(String text) {
              log.info("[{}] on text: {}", name, text);
              JsonNode json = JsonUtils.deserialize(text, JsonNode.class);
              if (name.equals(json.get("from").asText())) {
                synchronized (textQ) {
                  textQ.offer(text);
                  if (textQ.size() >= N) {
                    textQ.notify();
                  }
                }
              }
            }

            @Override
            public void onBinary(ByteString bytes) {
              log.info("[{}] on bytes: {}", name, bytes);
              synchronized (binaryQ) {
                binaryQ.offer(bytes);
                if (binaryQ.size() >= N) {
                  binaryQ.notify();
                }
              }
            }
          };
      client = WsClientFactory.buildClient(serverUri, listener);
    }

    @Override
    public void close() throws Exception {
      client.close();
    }
  }
}
