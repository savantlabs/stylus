package io.savantlabs.stylus.sdk.jupyter;

import java.util.List;

public interface JupyterClient {

  void stop();

  void installPackage(String pkgList) throws InterruptedException;
  void removePackage(String pkgList) throws InterruptedException;
  List<String> listPackage(String pkg) throws InterruptedException;
}
