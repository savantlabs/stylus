package io.savantlabs.stylus.sdk.jupyter;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class JupyterClientImplTest {

  @Test
  @SneakyThrows
  void checkInstallAndUninstallScript() {
    String packageName = "seaborn";
    try (JupyterClient client = JupyterLauncher.createClient()) {
      client.removePackage(packageName);
      assertFalse(client.hasPackage(packageName));
      client.installPackage(packageName);
      client.installPackage(packageName);
      assertTrue(client.hasPackage(packageName));
      client.removePackage(packageName);
      client.removePackage(packageName);
      assertFalse(client.hasPackage(packageName));
    }
  }
  @Test
  @SneakyThrows
  void checkExecutePythonCode() throws InterruptedException {
    JupyterClient client = JupyterLauncher.createClient();
    JupyterKernel kernel = client.startKernel();
    final String kernelId = kernel.getId();
    String code = "1 + 1";
    int check = client.executePythonCode(code, kernelId);
    assertEquals(2, check);
  }

}
