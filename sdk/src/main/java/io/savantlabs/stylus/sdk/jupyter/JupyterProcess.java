package io.savantlabs.stylus.sdk.jupyter;

import java.net.URI;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class JupyterProcess {

  @NonNull private Process process;

  private long jupyterPID = -1;
  private boolean stopPipe; // do not save messages in the queue
  private Thread shutdownHook;
  private boolean stopped;

  private final Deque<String> pipe = new LinkedList<>();

  @SneakyThrows
  public JupyterProcess() {
    process =
        ShellRunner.runScript(
            (p) ->
                (line) -> {
                  log.info("jupyter process [{}]: {}", p.pid(), line);
                  if (!stopPipe) {
                    pipe.push(line);
                  }
                },
            (p) ->
                (line) -> {
                  log.warn("jupyter process [{}]: {}", p.pid(), line);
                  if (!stopPipe) {
                    pipe.push(line);
                  }
                },
            "start_jupyter.sh");
    log.info("Started the jupyter process {} ...", process.pid());
    shutdownHook = new Thread(this::stop);
    Runtime.getRuntime().addShutdownHook(shutdownHook);
    // TODO: extract JUPYTER_PID from `pipe`, which is useful when stopping the process
  }

  @SneakyThrows
  void stop() {
    if (!stopped) {
      if (shutdownHook != null) {
        Runtime.getRuntime().removeShutdownHook(shutdownHook);
        shutdownHook = null;
      }
      if (jupyterPID > 0) {
        log.info("Tearing down the jupyter server {} ...", jupyterPID);
        Process process1 =
            ShellRunner.runCommand("/bin/bash", "-c", String.format("kill -9 %d", jupyterPID));
        process1.waitFor(1, TimeUnit.MINUTES);
      }
      try {
        long pid = process.pid();
        log.info("Stopping the jupyter process {} ...", pid);
        process.destroy();
        process.waitFor(1, TimeUnit.MINUTES);
      } catch (Exception e) {
        log.warn("Failed to shutdown jupyter process.", e);
      } finally {
        stopped = true;
      }
    }
  }

  URI extractServerUri(long timeout) {
    // TODO: read `pipe`, find http uri, then clear the pipe,
    // TODO: throw IllegalStateException if cannot find the uri after timeout (milliseconds)
    // TODO: stop persisting further messages
    stopPersistingMessages();
    return URI.create("http://localhost");
  }

  private void stopPersistingMessages() {
    pipe.clear();
    stopPipe = false;
  }
}
