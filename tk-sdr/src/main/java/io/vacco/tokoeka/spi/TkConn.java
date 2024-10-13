package io.vacco.tokoeka.spi;

import io.vacco.tokoeka.util.TkSocketState;
import java.net.Socket;
import java.util.function.Consumer;

public interface TkConn extends Consumer<String> {

  void setAttachment(Object attachment);
  <T> T getAttachment();

  Socket getSocket();
  TkSocketState getState();

  void close(int code);
  void close(int code, String msg);

}
