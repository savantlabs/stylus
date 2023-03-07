package io.savantlabs.stylus.sdk.jupyter;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
public class JupyterLauncherTests {

  @Test
  @SneakyThrows
  void testStartAndStopJupyter() {

    //storing list of initial processes
    List<String> initialProcesses = new ArrayList<>();
    Process process = ShellRunner.runCommand(
        (p) ->
            (line) -> {
              if(!line.contains("grep jupyter"))
                initialProcesses.add(line.split(" ")[1]);
            },
        (p) ->
            (line) -> {
            },
        "/bin/bash", "-c", "ps aux | grep jupyter"); //attach consumer
    process.waitFor();

    JupyterClient client = JupyterLauncher.createClient();
    Thread.sleep(5000);
    client.stop();


    //storing list of after processes
    List<String> afterProcesses = new ArrayList<>();
    Process process2 = ShellRunner.runCommand(
        (p) ->
            (line) -> {
              if(!line.contains("grep jupyter"))
                afterProcesses.add(line.split(" ")[1]);
            },
        (p) ->
            (line) -> {
            },
        "/bin/bash", "-c", "ps aux | grep jupyter"); //attach consumer
    process2.waitFor();

    //sorting the PIDs
    String[] initialPIDs = initialProcesses.toArray(new String[initialProcesses.size()]);
    String[] afterPIDs = afterProcesses.toArray(new String[afterProcesses.size()]);
    Arrays.sort(initialPIDs);
    Arrays.sort(afterPIDs);

    //validating the initial and after process count and the PIDs
    assert initialProcesses.size()==afterProcesses.size();
    assert Arrays.equals(initialPIDs,afterPIDs);
  }

}
