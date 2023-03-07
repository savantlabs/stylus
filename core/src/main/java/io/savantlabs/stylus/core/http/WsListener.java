package io.savantlabs.stylus.core.http;

import okio.ByteString;

public interface WsListener {

  void onText(String text);

  void onBinary(ByteString bytes);
}
