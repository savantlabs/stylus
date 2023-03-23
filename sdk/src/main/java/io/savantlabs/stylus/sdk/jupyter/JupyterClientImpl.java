package io.savantlabs.stylus.sdk.jupyter;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class JupyterClientImpl implements JupyterClient {

  @NonNull
  private final URI httpUri;

  @Getter(AccessLevel.NONE)
  private final JupyterProcess process;

  @SneakyThrows
  JupyterClientImpl() {
    process = new JupyterProcess();
    httpUri = process.extractServerUri(TimeUnit.MINUTES.toMillis(1));
  }

  @Override
  public void stop() {
    process.stop();
  }

  @Override
  public void installPackage(String installPkgsList) throws InterruptedException {
    Process process =
        ShellRunner.runScript(
            (p) ->
                (line) -> {
                  log.info("[Install] "+line);
                },
            (p) ->
                (line) -> {
                },
            "install_pkg.sh",installPkgsList);
    process.waitFor();
  }

  @Override
  public void removePackage(String removePkgsList) throws InterruptedException {
    Process process =
        ShellRunner.runScript(
            (p) ->
                (line) -> {
                  log.info("[Remove] "+line);
                },
            (p) ->
                (line) -> {
                },
            "uninstall_pkg.sh",removePkgsList);
    process.waitFor();
  }

  @Override
  public List<String> listPackage(String pkg) throws InterruptedException {
    List<String> pkgList = new ArrayList<>();
    AtomicBoolean foundFlag = new AtomicBoolean(false);
    Process process =
        ShellRunner.runScript(
            (p) ->
                (line) -> {
                  if(line.startsWith("#") && line.contains("packages in environment at")){
                    foundFlag.set(true);
                  }else if(!line.startsWith("#") && foundFlag.get()){
                    pkgList.add(line.split(" ")[0]);
                  }
                  log.info("[List] "+line);
                },
            (p) ->
                (line) -> {
                },
            "list_pkg.sh",pkg);
    process.waitFor();
    return pkgList;
  }



}
