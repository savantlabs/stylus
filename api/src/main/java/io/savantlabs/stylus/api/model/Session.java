package io.savantlabs.stylus.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Setter
@Getter
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Session {

  String id;
  String kernelId;

  public Session(String id, String kernelId) {
    this.id = id;
    this.kernelId = kernelId;
  }
}
