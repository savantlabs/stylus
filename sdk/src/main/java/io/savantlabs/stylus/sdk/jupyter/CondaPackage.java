package io.savantlabs.stylus.sdk.jupyter;

import lombok.Data;

@Data
public class CondaPackage {
  // List<String> properties; //list has 3 el -> name, version, channel
  String name;
  String version;
  String channel;
}
