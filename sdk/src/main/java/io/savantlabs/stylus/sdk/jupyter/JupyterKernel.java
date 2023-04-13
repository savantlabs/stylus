package io.savantlabs.stylus.sdk.jupyter;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;

@Value
@Builder(toBuilder = true)
@Jacksonized
@Slf4j
public class JupyterKernel {
  String id;
  String name;

  @JsonProperty("execution_state")
  String state;

  Integer connections;

  @JsonProperty("last_activity")
  String lastActivityStr; // to be converted to LocalDateTime
}
