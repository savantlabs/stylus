package io.savantlabs.stylus.sdk.jupyter;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Objects;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JupyterClientImplTest {

  CondaPackage cp = new CondaPackage();

  @Test
  @SneakyThrows
  void checkInstallAndUninstallScript() {
    String packageName = "seaborn";
    JupyterClient client = JupyterLauncher.createClient();
    client.removePackage(packageName);
    assertFalse(cp.hasPackage(packageName));
    client.installPackage(packageName);
    client.installPackage(packageName);
    assertTrue(cp.hasPackage(packageName));
    client.removePackage(packageName);
    client.removePackage(packageName);
    assertFalse(cp.hasPackage(packageName));
  }
}