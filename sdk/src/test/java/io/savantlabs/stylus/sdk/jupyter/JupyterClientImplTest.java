package io.savantlabs.stylus.sdk.jupyter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

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
}
