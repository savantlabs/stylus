package io.savantlabs.stylus.sdk.jupyter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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
    System.out.println("Printing existing processes");
    for (String x : initialProcesses) {
      System.out.println("Process" + x);
    }

    JupyterClient client = JupyterLauncher.createClient();
    URI httpUri = ((JupyterClientImpl) client).getHttpUri();
    System.out.println(httpUri);
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
    String[] cmd = {"/bin/bash", "-c", "ps aux | grep jupyter"};
    Runtime r = Runtime.getRuntime();
    Process p = r.exec(cmd);
    p.waitFor();
    BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
    String line = "";
    System.out.println("Printing existing processes");
    while ((line = b.readLine()) != null) {
      if (!line.contains("grep jupyter")) processes.add(line.split(" ")[3]);
      System.out.println(line);
    }

    b.close();
    //    Process process =
    //        ShellRunner.runCommand(
    //            (p) ->
    //                (line) -> {
    //                  if (!line.contains("grep jupyter")) processes.add(line.split(" ")[3]);
    //                },
    //            (p) -> (line) -> {
    //              System.out.println("!!!!!Line" + line);
    //            },
    //            "/bin/bash",
    //            "-c",
    //            "ps aux | grep jupyter"); // attach consumer
    // process.waitFor();
    return processes;
  }
}
