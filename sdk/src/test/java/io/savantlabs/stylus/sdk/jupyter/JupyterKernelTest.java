package io.savantlabs.stylus.sdk.jupyter;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JupyterKernelTest
{
  private boolean assertFlag(JsonNode res, String kernelId)
  {
    boolean flag = false;
    for(var node: res)
    {
      if(node.get("id").textValue().equals(kernelId))
      {
        flag = true;
        break;
      }
    }
    return flag;
  }

  @Test
  @SneakyThrows
  void startStopKernel()
  {
    JupyterClient client = JupyterLauncher.createClient();
    URI jupyterURL = ((JupyterClientImpl) client).getHttpUri();
    JsonNode res = client.listKernels(jupyterURL.toURL());
    log.info(res.toString());

    // kernel started
    client.startKernel();
    String kernelId = ((JupyterClientImpl) client).getKernelId();

    JupyterKernel obj = new JupyterKernel(jupyterURL);
    res = client.listKernels(jupyterURL.toURL());
    log.info(res.toString());

    // assert id true
    boolean flag = assertFlag(res, kernelId);

    Assertions.assertTrue(flag);
    obj.stopKernel(kernelId);
    res = client.listKernels(jupyterURL.toURL());
    log.info(res.toString());

    // assert id false
    flag = assertFlag(res, kernelId);
    Assertions.assertFalse(flag);
    client.stop();
  }

  @Test
  @SneakyThrows
  void startInterruptKernel()
  {
    JupyterClient client = JupyterLauncher.createClient();
    URI jupyterURL = ((JupyterClientImpl) client).getHttpUri();
    client.startKernel();
    String kernelId = ((JupyterClientImpl) client).getKernelId();
    JupyterKernel obj = new JupyterKernel(jupyterURL);
    JsonNode res = obj.getKernelDetails(kernelId);
    obj.interruptKernel(kernelId);
    res = obj.getKernelDetails(kernelId);
    obj.stopKernel(kernelId);
    client.stop();
  }
}
