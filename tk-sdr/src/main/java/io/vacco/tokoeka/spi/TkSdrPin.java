package io.vacco.tokoeka.spi;

public interface TkSdrPin {
  void onEvent(
    TkConn conn, String key, String value,
    Exception e, boolean ping, boolean pong
  );
}
