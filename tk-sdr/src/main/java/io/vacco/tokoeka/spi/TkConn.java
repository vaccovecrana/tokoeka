package io.vacco.tokoeka.spi;

import java.net.Socket;
import java.util.function.Consumer;

public interface TkConn extends Consumer<String> {
  void setAttachment(Object attachment);
  <T> T getAttachment();
  Socket getSocket();
  void close(int code);
  void close(int code, String msg);
}
