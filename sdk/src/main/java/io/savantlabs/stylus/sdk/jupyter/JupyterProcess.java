package io.savantlabs.stylus.sdk.jupyter;

import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@Getter
@Setter
public class JupyterProcess {

  private static final Pattern JUPYTER_PID_PATTERN = Pattern.compile("^JUPYTER_PID=(?<pid>\\d+)$");
  private static final Pattern JUPYTER_URL_HINT =
      Pattern.compile(".*Jupyter Notebook.*is running at:");
  private static final Pattern JUPYTER_URL =
      Pattern.compile("(?<url>http:\\/\\/[a-zA-Z0-9]+:[0-9]{1,5}\\/\\?token=[a-zA-Z0-9]+)");
  @NonNull private Process process;

  private final Object jupyterPIDMutex = new Object();
  private long jupyterPID = -1;
  private String jupyterURL = "";

  private final Object jupyterURLMutex = new Object();
  private Thread shutdownHook;
  private boolean stopped;

  @SneakyThrows
  public JupyterProcess() {
    AtomicBoolean next_line_is_url = new AtomicBoolean(false);
    process =
        ShellRunner.runScript(
            (p) ->
                (line) -> {
                  log.info("jupyter process [{}]: {}", p.pid(), line);
                  if (jupyterPID < 0) {
                    Matcher matcher = JUPYTER_PID_PATTERN.matcher(line);
                    if (matcher.matches()) {
                      jupyterPID = Long.parseLong(matcher.group("pid"));
                      log.info("Found jupyter pid from console output: {}", jupyterPID);
                      synchronized (jupyterPIDMutex) {
                        jupyterPIDMutex.notify();
                      }
                    }
                  }
                },
            (p) ->
                (line) -> {
                  log.warn("jupyter process [{}]: {}", p.pid(), line);
                  Matcher matcher = JUPYTER_URL_HINT.matcher(line);
                  if (matcher.matches()) {
                    // if(line.contains("Jupyter Notebook") && line.endsWith("is running at:")){
                    // set found_url to true since next line will be containing url
                    next_line_is_url.set(true);
                  } else if (next_line_is_url.get()) {
                    // found the line containing the url
                    matcher = JUPYTER_URL.matcher(line);
                    if (matcher.find()) {
                      jupyterURL = matcher.group("url");
                      log.info("Jupyter URL from console output: {}", jupyterURL);
                      next_line_is_url.set(false);
                      synchronized (jupyterURLMutex) {
                        jupyterURLMutex.notify();
                      }
                    }
                  }
                },
            "start_jupyter.sh");

    log.info("Started the jupyter process {} ...", process.pid());
    shutdownHook = new Thread(this::stop);
    Runtime.getRuntime().addShutdownHook(shutdownHook);
    if (jupyterPID < 0) {
      synchronized (jupyterPIDMutex) {
        jupyterPIDMutex.wait(TimeUnit.MINUTES.toMillis(1));
      }
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

  URI extractServerUri(long timeout) throws TimeoutException {
    if (StringUtils.isBlank(jupyterURL)) {
      long startTime = System.currentTimeMillis();
      synchronized (jupyterURLMutex) {
        while (StringUtils.isBlank(jupyterURL)
            && System.currentTimeMillis() - startTime < timeout) {
          try {
            jupyterURLMutex.wait(timeout);
          } catch (InterruptedException e) {
            log.warn("Waiting is interrupted", e);
          }
        }
        if (StringUtils.isBlank(jupyterURL)) {
          throw new TimeoutException("Failed to detect jupyter server URL within the timeout.");
        }
      }
    }
    return URI.create(jupyterURL);
  }
}
