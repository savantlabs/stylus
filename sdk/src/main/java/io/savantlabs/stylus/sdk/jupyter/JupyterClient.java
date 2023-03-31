package io.savantlabs.stylus.sdk.jupyter;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.URL;
import java.util.List;

public interface JupyterClient {

  void stop();

  void installPackage(String pkgList) throws InterruptedException;
  void removePackage(String pkgList) throws InterruptedException;
  List<String> listPackage(String pkg) throws InterruptedException;
  void startKernel() throws InterruptedException;
  public JsonNode listKernels(URL jupyterURL);
}
