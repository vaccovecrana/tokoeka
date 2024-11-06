package io.vacco.tokoeka.spi;

import java.nio.ByteBuffer;

public interface TkSocketHdl {

  void onOpen(TkConn conn, String handShake);
  void onClose(TkConn conn);
  void onMessage(TkConn conn, String msg);
  void onMessage(TkConn conn, ByteBuffer msg);
  void onPing(TkConn conn);
  void onPong(TkConn conn);
  void onError(TkConn conn, Exception e);

}
