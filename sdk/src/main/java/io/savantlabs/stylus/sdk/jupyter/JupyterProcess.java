package io.savantlabs.stylus.sdk.jupyter;

import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class JupyterProcess {

  private static final Pattern JUPYTER_PID_PATTERN = Pattern.compile("^JUPYTER_PID=(?<pid>\\d+)$");

  @NonNull private Process process;

  private final AtomicLong jupyterPID = new AtomicLong(-1);
  private boolean stopPipe; // do not save messages in the queue
  private Thread shutdownHook;
  private boolean stopped;

  @SneakyThrows
  public JupyterProcess() {
    process =
        ShellRunner.runScript(
            (p) ->
                (line) -> {
                  log.info("jupyter process [{}]: {}", p.pid(), line);
                  synchronized (jupyterPID) {
                    if (jupyterPID.get() < 0) {
                      Matcher matcher = JUPYTER_PID_PATTERN.matcher(line);
                      if (matcher.matches()) {
                        long pid = Long.parseLong(matcher.group("pid"));
                        log.info("Found jupyter pid from console output: {}", pid);
                        jupyterPID.set(pid);
                        jupyterPID.notify();
                      }
                    }
                  }
                },
            (p) ->
                (line) -> {
                  log.warn("jupyter process [{}]: {}", p.pid(), line);
                },
            "start_jupyter.sh");

    log.info("Started the jupyter process {} ...", process.pid());
    shutdownHook = new Thread(this::stop);
    Runtime.getRuntime().addShutdownHook(shutdownHook);
    // TODO: extract JUPYTER_PID from `pipe`, which is useful when stopping the process
    synchronized (jupyterPID) {
      jupyterPID.wait(TimeUnit.MINUTES.toMillis(1));
    }
    log.info("Jupyter client is fully initialized.");
  }

  @SneakyThrows
  void stop() {
    if (!stopped) {
      if (shutdownHook != null) {
        Runtime.getRuntime().removeShutdownHook(shutdownHook);
        shutdownHook = null;
      }
      if (jupyterPID.get() > 0) {
        log.info("Tearing down the jupyter server {} ...", jupyterPID.get());
        Process process1 =
            ShellRunner.runCommand(
                "/bin/bash", "-c", String.format("kill -9 %d", jupyterPID.get()));
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
    return URI.create("http://localhost");
  }
}
