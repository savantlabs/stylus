package io.savantlabs.stylus.sdk.jupyter;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class JupyterLauncherTests {

  @Test
  @SneakyThrows
  void testStartAndStopJupyter() {
    JupyterClient client = JupyterLauncher.createClient();
    Thread.sleep(5000);
    client.stop();
  }
}
