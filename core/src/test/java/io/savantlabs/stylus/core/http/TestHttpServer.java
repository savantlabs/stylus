package io.savantlabs.stylus.core.http;

import static io.undertow.Handlers.websocket;
import static io.undertow.util.Methods.DELETE;
import static io.undertow.util.Methods.GET;
import static io.undertow.util.Methods.POST;
import static io.undertow.util.Methods.PUT;

import com.fasterxml.jackson.databind.JsonNode;
import io.savantlabs.stylus.core.util.JsonUtils;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.PathHandler;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import io.undertow.websockets.WebSocketProtocolHandshakeHandler;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedBinaryMessage;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;

@Slf4j
public class TestHttpServer implements AutoCloseable {

  static final String TOKEN = "12345";

  private final Undertow server;
  @Getter private final int port;

  final ConcurrentLinkedQueue<JsonNode> jsonMessages = new ConcurrentLinkedQueue<>();
  final ConcurrentLinkedQueue<JsonNode> binaryMessages = new ConcurrentLinkedQueue<>();

  @Override
  public void close() {
    synchronized (TestHttpServer.class) {
      server.stop();
    }
  }

  TestHttpServer() {
    synchronized (TestHttpServer.class) {
      port = findAvailablePort();
      server = getHttpServer(port);
      server.start();
    }
  }

  private Undertow getHttpServer(int port) {
    PathHandler handler =
        Handlers.path()
            .addExactPath("/200", get200Hdlr())
            .addExactPath(
                "/401", getErrorHdlr(StatusCodes.UNAUTHORIZED, StatusCodes.UNAUTHORIZED_STRING))
            .addExactPath("/403", getErrorHdlr(StatusCodes.FORBIDDEN, StatusCodes.FORBIDDEN_STRING))
            .addExactPath(
                "/502", getErrorHdlr(StatusCodes.BAD_GATEWAY, StatusCodes.BAD_GATEWAY_STRING))
            .addExactPath(
                "/503",
                getErrorHdlr(
                    StatusCodes.SERVICE_UNAVAILABLE, StatusCodes.SERVICE_UNAVAILABLE_STRING))
            .addExactPath("/500", get500Hdlr())
            .addPrefixPath("/ws", getWsHdlr());
    return Undertow.builder().addHttpListener(port, "localhost").setHandler(handler).build();
  }

  private HttpHandler get200Hdlr() {
    return exchange -> {
      if (isAuthenticated(exchange)) {
        if (Set.of(GET, POST, PUT, DELETE).contains(exchange.getRequestMethod())) {
          exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
          exchange.getResponseSender().send(JsonUtils.serialize(Map.of("status", "Ok!")));
        } else {
          throw new UnsupportedOperationException(
              exchange.getRequestMethod() + " is not supported at this endpoint.");
        }
      }
    };
  }

  private HttpHandler getErrorHdlr(int statusCode, String errorMsg) {
    return exchange -> {
      if (isAuthenticated(exchange)) {
        if (Set.of(GET, POST, PUT, DELETE).contains(exchange.getRequestMethod())) {
          exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
          exchange.setStatusCode(statusCode);
          exchange.getResponseSender().send(JsonUtils.serialize(Map.of("error_detail", errorMsg)));
        } else {
          throw new UnsupportedOperationException(
              exchange.getRequestMethod() + " is not supported at this endpoint.");
        }
      }
    };
  }

  private HttpHandler get500Hdlr() {
    return exchange -> {
      if (isAuthenticated(exchange)) {
        if (Set.of(GET, POST, PUT, DELETE).contains(exchange.getRequestMethod())) {
          throw new RuntimeException("Server failure");
        } else {
          throw new UnsupportedOperationException(
              exchange.getRequestMethod() + " is not supported at this endpoint.");
        }
      }
    };
  }

  private WebSocketProtocolHandshakeHandler getWsHdlr() {
    return websocket(
        (exchange, channel) -> {
          channel.getReceiveSetter().set(getListener());
          channel.resumeReceives();
        });
  }

  private AbstractReceiveListener getListener() {
    return new AbstractReceiveListener() {
      @Override
      protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) {
        String messageData = message.getData();
        log.info("On text message: {}", messageData);
        jsonMessages.offer(JsonUtils.deserialize(messageData, JsonNode.class));
        for (WebSocketChannel session : channel.getPeerConnections()) {
          WebSockets.sendText(messageData, session, null);
        }
      }

      @Override
      protected void onFullBinaryMessage(WebSocketChannel channel, BufferedBinaryMessage message) {
        ByteBuffer[] buffers = message.getData().getResource();
        ByteBuffer buffer = WebSockets.mergeBuffers(buffers);
        String str = new String(buffer.array(), StandardCharsets.UTF_8);
        log.info("On binary message: {}", str);
        binaryMessages.offer(JsonUtils.deserialize(str, JsonNode.class));
        WebSockets.sendBinary(buffer, channel, null);
      }
    };
  }

  private boolean isAuthenticated(HttpServerExchange exchange) {
    HeaderMap reqHeaders = exchange.getRequestHeaders();
    boolean authenticated = false;
    if (reqHeaders.contains(Headers.AUTHORIZATION_STRING)) {
      authenticated = ("Bearer " + TOKEN).equals(reqHeaders.getFirst(Headers.AUTHORIZATION_STRING));
    }
    if (!authenticated) {
      exchange.setStatusCode(401);
      exchange.getResponseSender().send("Unauthorized!");
    }
    return authenticated;
  }

  @SneakyThrows
  private int findAvailablePort() {
    try (ServerSocket serverSocket = new ServerSocket(0)) {
      Assertions.assertThat(serverSocket).isNotNull();
      Assertions.assertThat(serverSocket.getLocalPort()).isGreaterThan(0);
      return serverSocket.getLocalPort();
    }
  }
}
