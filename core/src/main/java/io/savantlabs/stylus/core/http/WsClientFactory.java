package io.savantlabs.stylus.core.http;

import java.net.URI;
import lombok.experimental.UtilityClass;

@UtilityClass
public class WsClientFactory {

  public static WsClient buildClient(URI baseUri, WsListener listener) {
    return new WsClientImpl(baseUri, listener);
  }
}
