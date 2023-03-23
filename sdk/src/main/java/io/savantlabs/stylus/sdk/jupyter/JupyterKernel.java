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

@Data
public class JupyterKernel
{

  private URI jupyterURL;

  private String tokenQuery;

  public static JupyterKernel createKernel(@NonNull URI jupyterURL) throws IOException {
    return new JupyterKernel(jupyterURL);
  }

  private JupyterKernel(URI jupyterURL) throws IOException {
    this.jupyterURL = jupyterURL;
    this.tokenQuery = jupyterURL.getQuery();
    this.startKernel();
  }

  public void startKernel() throws IOException
  {
    int port = -1;
    try
    {
      URI uri = new URI(jupyterURL.toString());
      port = uri.getPort();
    } catch(Exception e)
    {
      e.printStackTrace();
    }

    ApiProxy proxy = ApiProxyFactory.buildProxy(URI.create("http://localhost:" + port));
    JsonNode res = proxy.post("/api/kernels?"+this.tokenQuery,
        Map.of("name","python3","env","{ \"KERNEL_USERNAME\": \"jovyan\" }"),
        JsonNode.class);
    System.out.println("response"+res);
    String kernelId = res.get("id").textValue();
    System.out.println("kerned ID: "+kernelId);
    System.out.println("done");
  }
}
