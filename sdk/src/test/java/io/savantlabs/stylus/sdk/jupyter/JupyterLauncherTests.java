package io.savantlabs.stylus.sdk.jupyter;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class JupyterLauncherTests {

  @Test
  @SneakyThrows
  void testStartAndStopJupyter() {
    Process process = ShellRunner.runCommand("/bin/bash", "-c", "ps aux | grep jupyter");
    process.waitFor();
    JupyterClient client = JupyterLauncher.createClient();
    Thread.sleep(5000);
    client.stop();
    process = ShellRunner.runCommand("/bin/bash", "-c", "ps aux | grep jupyter");
    process.waitFor();
  }
}
