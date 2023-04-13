package io.savantlabs.stylus.sdk.jupyter;

import com.fasterxml.jackson.databind.JsonNode;
import io.savantlabs.stylus.core.http.ApiProxy;
import io.savantlabs.stylus.core.http.ApiProxyFactory;
import io.savantlabs.stylus.core.util.JsonUtils;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class JupyterClientImpl implements JupyterClient {

  @NonNull private final URI httpUri;

  @Getter(AccessLevel.NONE)
  private final JupyterProcess process;

  private final URI jupyterURL;
  private final String tokenQuery;

  private volatile ApiProxy apiProxy;

  @SneakyThrows
  JupyterClientImpl() {
    process = new JupyterProcess();
    httpUri = process.extractServerUri(TimeUnit.MINUTES.toMillis(3));
    jupyterURL = httpUri;
    tokenQuery = jupyterURL.getQuery();
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
