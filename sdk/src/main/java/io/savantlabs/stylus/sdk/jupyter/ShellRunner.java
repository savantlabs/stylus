package io.savantlabs.stylus.sdk.jupyter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;

@Slf4j
@UtilityClass
public class ShellRunner {

  @SneakyThrows
  static Process runScript(
      Function<Process, Consumer<String>> outStreamConsumerProvider,
      Function<Process, Consumer<String>> errStreamConsumerProvider,
      String scriptFile) {
    return runScript(outStreamConsumerProvider,errStreamConsumerProvider,scriptFile,"");
  }

  @SneakyThrows
  static Process runScript(
      Function<Process, Consumer<String>> outStreamConsumerProvider,
      Function<Process, Consumer<String>> errStreamConsumerProvider,
      String scriptFile, String parameter_list) {
    String path = writeTempScripts(scriptFile);
    try {
      ProcessBuilder builder = new ProcessBuilder();
      builder.command("/bin/bash", path, parameter_list);
      System.out.println(String.join(" ",builder.command().toArray(new String[0])));
      Process process = builder.start();
      pipeStreams(
          process,
          outStreamConsumerProvider.apply(process),
          errStreamConsumerProvider.apply(process));
      return process;
    } finally {
      Thread hook =
          new Thread(
              () -> {
                log.info("Deleting temporary script file {}", path);
                FileUtils.deleteQuietly(new File(path));
              });
      Runtime.getRuntime().addShutdownHook(hook);
    }
  }


  @SneakyThrows
  static Process runCommand(
      Function<Process, Consumer<String>> outStreamConsumerProvider,
      Function<Process, Consumer<String>> errStreamConsumerProvider,
      String... command) {
    ProcessBuilder builder = new ProcessBuilder();
    builder.command(command);
    Process process = builder.start();
    pipeStreams(
        process,
        outStreamConsumerProvider.apply(process),
        errStreamConsumerProvider.apply(process));
    return process;
  }

  @SneakyThrows
  public static Process runCommand(String... command) {
    ProcessBuilder builder = new ProcessBuilder();
    builder.command(command);
    Process process = builder.start();
    pipeStreams(process);
    return process;
  }

  private void pipeStreams(Process process) {
    Consumer<String> outStreamConsumer =
        (line) -> log.info("process [{}]: {}", process.pid(), line);
    Consumer<String> errStreamConsumer =
        (line) -> log.warn("process [{}]: {}", process.pid(), line);
    pipeStreams(process, outStreamConsumer, errStreamConsumer);
  }

  private void pipeStreams(
      Process process, Consumer<String> outStreamConsumer, Consumer<String> errStreamConsumer) {
    StreamGobbler outStreamGobbler = new StreamGobbler(process.getInputStream(), outStreamConsumer);
    StreamGobbler errStreamGobbler = new StreamGobbler(process.getErrorStream(), errStreamConsumer);
    Future<?> outGobblerFuture = Executors.newSingleThreadExecutor().submit(outStreamGobbler);
    Future<?> errGobblerFuture = Executors.newSingleThreadExecutor().submit(errStreamGobbler);
    process
        .onExit()
        .thenAccept(
            (p) -> {
              try {
                outGobblerFuture.get(1, TimeUnit.MINUTES);
              } catch (Exception e) {
                log.warn("Failed to shutdown stdout stream gobbler", e);
              }
              try {
                errGobblerFuture.get(1, TimeUnit.MINUTES);
              } catch (Exception e) {
                log.warn("Failed to shutdown sterr stream gobbler", e);
              }
            });
  }

  @SneakyThrows
  private String writeTempScripts(String scriptFile) {
    String tempScript = ".tmp/" + RandomStringUtils.randomAlphabetic(8).toLowerCase() + ".sh";
    InputStream is =
        Objects.requireNonNull(
            Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("scripts/" + scriptFile));
    // assert statement
    FileUtils.forceMkdirParent(new File(tempScript));
    IOUtils.copy(is, new FileOutputStream(tempScript));
    return tempScript;
  }

  private class StreamGobbler implements Runnable {
    private final InputStream inputStream;
    private final Consumer<String> consumer;

    StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
      this.inputStream = inputStream;
      this.consumer = consumer;
    }

    @Override
    public void run() {
      new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumer);
    }
  }
}
