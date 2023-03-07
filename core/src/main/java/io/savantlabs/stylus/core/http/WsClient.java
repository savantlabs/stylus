package io.savantlabs.stylus.core.http;

import okio.ByteString;

public interface WsClient extends AutoCloseable {

  boolean sendJson(Object body);

  boolean sendBytes(ByteString bytes);
}
