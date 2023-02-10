package io.savantlabs.stylus.sdk.jupyter;

import java.net.URI;
import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@Builder
public class JupyterClientImpl implements JupyterClient {

  @NonNull private final URI httpUri;

  @Getter(AccessLevel.NONE)
  private final JupyterProcess process;

  JupyterClientImpl() {
    process = new JupyterProcess();
    httpUri = process.extractServerUri(TimeUnit.MINUTES.toMillis(1));
  }

  @Override
  public void stop() {
    process.stop();
  }
}
