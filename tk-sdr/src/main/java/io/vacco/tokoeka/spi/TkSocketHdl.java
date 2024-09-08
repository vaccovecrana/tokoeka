package io.vacco.tokoeka.spi;

import java.nio.ByteBuffer;

public interface TkSocketHdl {

  void onOpen(String handShake);
  void onMessage(String message);
  void onMessage(ByteBuffer message);
  void onClose(int code);
  void onError(Exception e);

}
