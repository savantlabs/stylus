package io.savantlabs.stylus.core.http;

import io.savantlabs.stylus.core.util.JsonUtils;
import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Slf4j
public class WsClientImpl implements WsClient {

  private static final int NORMAL_CLOSURE_STATUS = 1000;
  private final AtomicReference<WebSocket> webSocket = new AtomicReference<>();
  private final String baseUri;
  private final WsListener listener;

  public WsClientImpl(URI baseUri, WsListener listener) {
    this.baseUri = baseUri.toString();
    this.listener = listener;
    connect();
  }

  @Override
  public boolean sendJson(Object body) {
    return webSocket.get().send(JsonUtils.serialize(body));
  }

  @Override
  public boolean sendBytes(ByteString bytes) {
    return webSocket.get().send(bytes);
  }

  @Override
  public void close() {
    synchronized (webSocket) {
      if (webSocket.get() != null) {
        try {
          webSocket.get().close(NORMAL_CLOSURE_STATUS, "Close connection.");
        } catch (Exception e) {
          log.warn("Error when closing the previous websocket");
        }
        webSocket.set(null);
      }
    }
  }

  private void connect() {
    if (webSocket.get() != null) {
      close();
    }
    synchronized (webSocket) {
      if (webSocket.get() == null) {
        Request request = new Request.Builder().url(baseUri).build();
        OkHttpClient client = HttpClientFactory.getKeepAliveHttpClient();
        webSocket.set(client.newWebSocket(request, getListener()));
        client.dispatcher().executorService().shutdown(); // to avoid memory leak
      }
    }
  }

  private WebSocketListener getListener() {
    return new WebSocketListener() {
      @Override
      @SneakyThrows
      public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
        super.onOpen(webSocket, response);
        log.info("WebSocket connection opened");
        log.info(
            "On open - {}: {}",
            response.code(),
            response.body() != null ? response.body().string() : "");
      }

      @Override
      public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
        super.onMessage(webSocket, text);
        listener.onText(text);
      }

      @Override
      public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
        super.onMessage(webSocket, bytes);
        listener.onBinary(bytes);
      }

      @Override
      public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
        super.onClosing(webSocket, code, reason);
        log.info("On closing - {}: {}", code, reason);
      }

      @Override
      public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
        super.onClosed(webSocket, code, reason);
        log.info("On closed - {}: {}", code, reason);
      }

      @Override
      public void onFailure(
          @NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
        super.onFailure(webSocket, t, response);
        log.warn("WebSocket connection failed", t);
        log.warn("On failure, reconnect ...", t);
        connect();
      }
    };
  }
}
