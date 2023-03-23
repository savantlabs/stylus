//package io.savantlabs.stylus.sdk.jupyter;
//
//import java.net.URI;
//import lombok.SneakyThrows;
//import org.junit.jupiter.api.Test;
//
//public class JupyterKernelTest
//{
//  @Test
//  @SneakyThrows
//  void startKernel()
//  {
//    JupyterClient client = JupyterLauncher.createClient();
//    URI jupyterURL = ((JupyterClientImpl) client).getHttpUri();
//    JupyterKernel jk = new JupyterKernel(jupyterURL);
//    jk.startKernel();
//    Thread.sleep(3000);
//    client.stop();
//  }
//}
