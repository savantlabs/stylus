package io.savantlabs.stylus.sdk.jupyter;

import com.fasterxml.jackson.databind.JsonNode;
import io.savantlabs.stylus.core.http.ApiProxy;
import io.savantlabs.stylus.core.http.ApiProxyFactory;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
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

  @NonNull
  private final URI httpUri;

  @Getter(AccessLevel.NONE)
  private final JupyterProcess process;
  private URI jupyterURL;
  private String tokenQuery;
  private String kernelId = "";

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
  public void installPackage(String installPkgsList) throws InterruptedException {
    Process process =
        ShellRunner.runScript(
            (p) ->
                (line) -> {
                  log.info("[Install] "+line);
                },
            (p) ->
                (line) -> {
                },
            "install_pkg.sh",installPkgsList);
    process.waitFor();
  }

  @Override
  public void removePackage(String removePkgsList) throws InterruptedException {
    Process process =
        ShellRunner.runScript(
            (p) ->
                (line) -> {
                  log.info("[Remove] "+line);
                },
            (p) ->
                (line) -> {
                },
            "uninstall_pkg.sh",removePkgsList);
    process.waitFor();
  }

  @Override
  public List<String> listPackage(String pkg) throws InterruptedException {
    List<String> pkgList = new ArrayList<>();
    AtomicBoolean foundFlag = new AtomicBoolean(false);
    Process process =
        ShellRunner.runScript(
            (p) ->
                (line) -> {
                  if(line.startsWith("#") && line.contains("packages in environment at")){
                    foundFlag.set(true);
                  }else if(!line.startsWith("#") && foundFlag.get()){
                    pkgList.add(line.split(" ")[0]);
                  }
                  log.info("[List] "+line);
                },
            (p) ->
                (line) -> {
                },
            "list_pkg.sh",pkg);
    process.waitFor();
    return pkgList;
  }
  @Override
  public void startKernel()
  {
    int port = jupyterURL.getPort();

    ApiProxy proxy = ApiProxyFactory.buildProxy(URI.create("http://localhost:" + port));
    JsonNode res = proxy.post("/api/kernels?"+this.tokenQuery,
        Map.of("name","python3","env","{ \"KERNEL_USERNAME\": \"jovyan\" }"),
        JsonNode.class);
    kernelId = res.get("id").textValue();
    log.info("Kernel Started...");
  }
  @Override
  public JsonNode listKernels(URL jupyterURL)
  {
    int port = jupyterURL.getPort();

    String tokenQuery = jupyterURL.getQuery();
    ApiProxy proxy = ApiProxyFactory.buildProxy(URI.create("http://localhost:" + port));
    JsonNode res = proxy.get("/api/kernels?"+tokenQuery, JsonNode.class);
    return res;
  }
}
