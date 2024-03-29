package io.savantlabs.stylus.sdk.jupyter;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JupyterLauncher {

  @SneakyThrows
  public static synchronized JupyterClient createClient() {
    return new JupyterClientImpl();
  }
}
