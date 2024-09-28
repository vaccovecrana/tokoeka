package io.vacco.tokoeka.spi;

import java.nio.ByteBuffer;

public interface TkSocketHdl {

  void onOpen(TkConn conn, String handShake);
  void onMessage(TkConn conn, String msg);
  void onMessage(TkConn conn, ByteBuffer msg);
  void onClose(TkConn conn, int code, boolean remote);
  void onError(TkConn conn, Exception e);

}
