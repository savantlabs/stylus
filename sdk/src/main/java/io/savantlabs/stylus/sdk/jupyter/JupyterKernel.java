package io.savantlabs.stylus.sdk.jupyter;
import com.fasterxml.jackson.databind.JsonNode;
import io.savantlabs.stylus.core.http.ApiProxy;
import io.savantlabs.stylus.core.http.ApiProxyFactory;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Data;
import java.net.URI;
import lombok.NonNull;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class JupyterKernel
{

  private URI jupyterURL;
  private String tokenQuery;

//  public static JupyterKernel createKernel(@NonNull URI jupyterURL)
//      throws IOException, InterruptedException {
//    return new JupyterKernel(jupyterURL);
//  }
//
   JupyterKernel(URI jupyterURL) throws IOException, InterruptedException {
    this.jupyterURL = jupyterURL;
    this.tokenQuery = jupyterURL.getQuery();
    //this.startKernel();
  }

//  public static JsonNode listKernels(URL jupyterURL)
//  {
//    int port = jupyterURL.getPort();
//
//    String tokenQuery = jupyterURL.getQuery();
//    ApiProxy proxy = ApiProxyFactory.buildProxy(URI.create("http://localhost:" + port));
//    JsonNode res = proxy.get("/api/kernels?"+tokenQuery, JsonNode.class);
//    return res;
//  }
//  private void startKernel() throws IOException
//  {
//    int port = jupyterURL.getPort();
//
//    ApiProxy proxy = ApiProxyFactory.buildProxy(URI.create("http://localhost:" + port));
//    JsonNode res = proxy.post("/api/kernels?"+this.tokenQuery,
//        Map.of("name","python3","env","{ \"KERNEL_USERNAME\": \"jovyan\" }"),
//        JsonNode.class);
//    System.out.println("response" + res);
//    kernelId = res.get("id").textValue();
//    System.out.println("kerned ID: "+kernelId);
//    System.out.println("Kernel STARTED!!");
//  }
  public void stopKernel(String kernelId) throws IOException, InterruptedException
  {
    int port = jupyterURL.getPort();
    ApiProxy proxy = ApiProxyFactory.buildProxy(URI.create("http://localhost:" + port));
    String res = proxy.delete("/api/kernels/"+ kernelId + "?" + this.tokenQuery, String.class);
    log.info("Kernel Stopped..." + res);
  }
  public void interruptKernel(String kernelId)
  {
    int port = jupyterURL.getPort();

    ApiProxy proxy = ApiProxyFactory.buildProxy(URI.create("http://localhost:" + port));
    String res = proxy.post("/api/kernels/"+ kernelId + "/interrupt?" + this.tokenQuery, Map.of(), String.class);
    log.info("Kernel Interrupted..." + res);
  }

  public JsonNode getKernelDetails(String kernelId)
  {
    int port = jupyterURL.getPort();

    ApiProxy proxy = ApiProxyFactory.buildProxy(URI.create("http://localhost:" + port));
    JsonNode res = proxy.get("/api/kernels/"+ kernelId +"/?"+ this.tokenQuery, JsonNode.class);
    log.info("Details fetched..." + res);
    return res;
  }
}
