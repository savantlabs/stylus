package io.savantlabs.stylus.sdk.jupyter;

import com.fasterxml.jackson.databind.JsonNode;
import io.savantlabs.stylus.core.http.*;
import io.savantlabs.stylus.core.util.JsonUtils;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Slf4j
public class JupyterClientImpl implements JupyterClient {

  @NonNull private final URI httpUri;

  @Getter(AccessLevel.NONE)
  private final JupyterProcess process;

  private final URI jupyterURL;
  private final String tokenQuery;
  private int ans = 0;

  private volatile ApiProxy apiProxy;

  @SneakyThrows
  public JupyterClientImpl() {
    process = new JupyterProcess();
    httpUri = process.extractServerUri(TimeUnit.MINUTES.toMillis(3));
    jupyterURL = httpUri;
    tokenQuery = jupyterURL.getQuery();
  }

  public int executePythonCode(String code, String kernelId) throws InterruptedException, TimeoutException {
//    int endIndex = httpUri.toString().indexOf('?');
//    if (httpUri.toString().charAt(endIndex - 1) == '/') {
//      endIndex--;
//    }
//    String baseUrl = httpUri.toString().substring(0, endIndex);
    //System.out.println(baseUrl);
//    URI uri = URI.create(httpUri.toString() + "/api/kernels/" + kernelId + "/channels");
//    System.out.println(uri.toString());
//    OkHttpClient client = new OkHttpClient();
//    Request request = new Request.Builder().url(uri.toString()).build();
//    CountDownLatch latch = new CountDownLatch(1);
//    WebSocketListener webSocketListener = new WebSocketListener()
//    {
//      @Override
//      public void onOpen(WebSocket webSocket, Response response)
//      {
//        log.info("WebSocket connection opened");
//
//        // Preparing the execute_request message content
//
//        Map<String, Object> executeRequestContent = new HashMap<>();
//        executeRequestContent.put("code", code);
//        executeRequestContent.put("silent", true);
//        executeRequestContent.put("store_history", false);
//        executeRequestContent.put("user_expressions", new HashMap<>());
//        executeRequestContent.put("allow_stdin", false);
//        executeRequestContent.put("stop_on_error", true);
//
//        // Preparing the execute_request message
//        Map<String, Object> executeRequest = new HashMap<>();
//        executeRequest.put("content", executeRequestContent);
//
//        // Converting the execute_request message to JSON
//        String executeRequestJson = JsonUtils.serialize(executeRequest);
//
//        // Send the execute_request message
//        webSocket.send(executeRequestJson);
//      }
//
//      @Override
//      public void onMessage(WebSocket webSocket, String text) {
//        log.info("Received message: " + text);
//        ans = Integer.parseInt(text.trim());
//        latch.countDown();
//      }
//
//      @Override
//      public void onClosed(WebSocket webSocket, int code, String reason) {
//        log.info("WebSocket connection closed");
//      }
//
//      @Override
//      public void onFailure(WebSocket webSocket, Throwable t, Response response) {
//        System.err.println("WebSocket error: " + t.getMessage());
//      }
//    };
//    WebSocket webSocket = client.newWebSocket(request, webSocketListener);
//    latch.await();
//    return ans;
    String wsUrl = httpUri.toString().replaceFirst("http://", "wss://");
    URI endpointUri = URI.create(wsUrl + "/api/kernels/" + kernelId + "/channels");
    log.info(endpointUri.toString());
    WsListenerImpl listener = new WsListenerImpl();
    WsClientImpl wsClient = new WsClientImpl(endpointUri, listener);
    Map<String, Object> executeRequestContent = new HashMap<>();
    executeRequestContent.put("code", code);
    executeRequestContent.put("silent", true);
    executeRequestContent.put("store_history", false);
    executeRequestContent.put("user_expressions", new HashMap<>());
    executeRequestContent.put("allow_stdin", false);
    executeRequestContent.put("stop_on_error", true);
    Map<String, Object> executeRequest = new HashMap<>();
    executeRequest.put("content", executeRequestContent);
    wsClient.sendJson(executeRequest);
    CountDownLatch latch = new CountDownLatch(1);
    listener.setLatch(latch);
    long timeout = 5000;
    boolean receivedResponse = latch.await(timeout, TimeUnit.MILLISECONDS);
    if (!receivedResponse) {
      throw new TimeoutException("Timeout occurred while waiting for the response");
    }
    //log.info("Successfully Sent Message");
    String response = listener.getResponse();
    wsClient.close();
    int result = Integer.parseInt(response.trim());
    return result;
  }

  @Override
  public void stop() {
    process.stop();
  }

  @Override
  public void close() {
    stop();
  }

  @SneakyThrows
  @Override
  public void installPackage(String installPkgsList) {
    Process process =
        ShellRunner.runScript(
            (p) ->
                (line) -> {
                  log.info("[Install] " + line);
                },
            (p) -> (line) -> {},
            "install_pkg.sh",
            installPkgsList);
    process.waitFor();
  }

  @SneakyThrows
  @Override
  public void removePackage(String removePkgsList) {
    Process process =
        ShellRunner.runScript(
            (p) ->
                (line) -> {
                  log.info("[Remove] " + line);
                },
            (p) -> (line) -> {},
            "uninstall_pkg.sh",
            removePkgsList);
    process.waitFor();
  }

  @SneakyThrows
  @Override
  public List<String> listPackage(String pkg) {
    List<String> pkgList = new ArrayList<>();
    AtomicBoolean foundFlag = new AtomicBoolean(false);
    Process process =
        ShellRunner.runScript(
            (p) ->
                (line) -> {
                  if (line.startsWith("#") && line.contains("packages in environment at")) {
                    foundFlag.set(true);
                  } else if (!line.startsWith("#") && foundFlag.get()) {
                    pkgList.add(line.split(" ")[0]);
                  }
                  log.info("[List] " + line);
                },
            (p) -> (line) -> {},
            "list_pkg.sh",
            pkg);
    process.waitFor();
    return pkgList;
  }

  @SneakyThrows
  @Override
  public boolean hasPackage(String name) {
    AtomicBoolean foundFlag =
        new AtomicBoolean(false); // found flag for whether package found or not
    AtomicBoolean checkFlag = new AtomicBoolean(false); // check flag for checking the lines
    Process process =
        ShellRunner.runScript(
            (p) ->
                (line) -> {
                  if (line.startsWith(
                      "# Name                    Version                   Build  Channel")) {
                    checkFlag.set(true);
                  } else if (checkFlag.get() && line.split(" ")[0].equals(name)) {
                    foundFlag.set(true);
                  }
                },
            (p) -> (line) -> {},
            "list_pkg.sh",
            name);
    process.waitFor();
    return foundFlag.get();
  }

  @SneakyThrows
  @Override
  public JupyterKernel startKernel() {
    ApiProxy proxy = getApiProxy();
    JsonNode res =
        proxy.post(
            getKernelEndpoint(""),
            Map.of("name", "python3", "env", "{ \"KERNEL_USERNAME\": \"jovyan\" }"),
            JsonNode.class);
    final String kernelId = res.get("id").asText();
    log.info("res: {}", JsonUtils.pprint(res));
//    while ("starting".equals(res.get("execution_state").asText())) {
//      log.info("Kernel status: {}", res.get("execution_state").asText());
//      //noinspection BusyWait
//      Thread.sleep(1000);
//      res = proxy.get(getKernelEndpoint("/" + kernelId), JsonNode.class);
//      log.info("res: {}", JsonUtils.pprint(res));
//    }
    log.info("Kernel {} started...", res.get("id"));
    return JsonUtils.deserialize(res, JupyterKernel.class);
  }

  @Override
  public void stopKernel(String kernelId) {
    ApiProxy proxy = getApiProxy();
    String res = proxy.delete(getKernelEndpoint(""), String.class);
    log.info("Kernel Stopped..." + res);
  }

  @Override
  public void interruptKernel(String kernelId) {
    ApiProxy proxy = getApiProxy();
    String res = proxy.post(getKernelEndpoint("/interrupt"), Map.of(), String.class);
    log.info("Kernel Interrupted..." + res);
  }

  @Override
  public JupyterKernel getKernel(String kernelId) {
    ApiProxy proxy = getApiProxy();
    JsonNode res = proxy.get(getKernelEndpoint("/"), JsonNode.class);
    log.info("resp: {}", res); // to be removed
    return JsonUtils.deserialize(res, JupyterKernel.class);
  }

  @Override
  public List<JupyterKernel> listKernels() {
    ApiProxy proxy = getApiProxy();
    String res = proxy.get(getRestEndpoint("/kernels"), String.class);
    log.info("resp: {}", res); // to be removed
    return JsonUtils.deserializeList(res, JupyterKernel.class);
  }

  private String getKernelEndpoint(String endpoint) {
    return getRestEndpoint("/kernels" + endpoint);
  }

  private String getRestEndpoint(String endpoint) {
    return "/api" + endpoint + "?" + tokenQuery;
  }

  private ApiProxy getApiProxy() {
    if (apiProxy == null) {
      synchronized (this) {
        if (apiProxy == null) {
          String protocol = jupyterURL.getScheme();
          String host = jupyterURL.getHost();
          int port = jupyterURL.getPort();
          if (("https".equalsIgnoreCase(protocol) && port == 443)
              || ("http".equalsIgnoreCase(protocol) && port == 80)) {
            apiProxy =
                ApiProxyFactory.buildProxy(URI.create(String.format("%s://%s", protocol, host)));
          } else {
            apiProxy =
                ApiProxyFactory.buildProxy(
                    URI.create(String.format("%s://%s:%d", protocol, host, port)));
          }
        }
      }
    }
    return apiProxy;
  }
}
