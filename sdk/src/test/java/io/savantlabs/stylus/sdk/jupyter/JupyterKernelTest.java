package io.savantlabs.stylus.sdk.jupyter;

import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class JupyterKernelTest {
  private boolean assertKernelIdExists(JupyterClient client, String kernelId) {
    List<JupyterKernel> kernels = client.listKernels();
    return kernels.stream().anyMatch(k -> kernelId.equals(k.getId()));
  }

  @Test
  @SneakyThrows
  void startStopKernel() {
    try (JupyterClient client = JupyterLauncher.createClient()) {
      // kernel started
      JupyterKernel kernel = client.startKernel();
      Assertions.assertThat(kernel).isNotNull();
      final String kernelId = kernel.getId();

      // assert kernel exist
      Assertions.assertThat(assertKernelIdExists(client, kernelId)).isTrue();

      client.stopKernel(kernelId);

      // assert kernel does not exist
      Assertions.assertThat(assertKernelIdExists(client, kernelId)).isFalse();
    }
  }

  @Test
  @SneakyThrows
  void startInterruptKernel() {
    try (JupyterClient client = JupyterLauncher.createClient()) {
      JupyterKernel kernel = client.startKernel();
      Assertions.assertThat(kernel).isNotNull();
      final String kernelId = kernel.getId();
      Assertions.assertThat(kernel.getState()).isEqualTo("running");
      client.interruptKernel(kernelId);
      JupyterKernel kernel2 = client.getKernel(kernelId);
      // FIXME: if possible, we want to compare kernel and kernel2
      client.stopKernel(kernelId);
    }
  }
}
