package io.savantlabs.stylus.sdk.jupyter;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class JupyterLauncherTests {

  @Test
  @SneakyThrows
  void testStartAndStopJupyter() {

    // get list of initial processes
    List<String> initialProcesses = listJupyterProcesses();

    JupyterClient client = JupyterLauncher.createClient();
    URI httpUri = ((JupyterClientImpl) client).getHttpUri();
    Assertions.assertThat(httpUri).isNotNull();
    Assertions.assertThat(httpUri.getHost()).isEqualTo("localhost");
    client.stop();

    // get list of after processes
    List<String> afterProcesses = listJupyterProcesses();
    Assertions.assertThat(afterProcesses).containsExactlyInAnyOrderElementsOf(initialProcesses);
  }

  @SneakyThrows
  private List<String> listJupyterProcesses() {
    List<String> processes = new ArrayList<>();
    Process process =
        ShellRunner.runCommand(
            (p) ->
                (line) -> {
                  if (!line.contains("grep jupyter")) processes.add(line.split(" ")[1]);
                },
            (p) -> (line) -> {},
            "/bin/bash",
            "-c",
            "ps aux | grep jupyter"); // attach consumer
    process.waitFor();
    return processes;
  }
}
