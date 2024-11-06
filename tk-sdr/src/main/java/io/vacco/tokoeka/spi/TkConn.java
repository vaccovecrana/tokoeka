package io.vacco.tokoeka.spi;

import io.vacco.tokoeka.util.TkSocketState;
import java.io.IOException;
import java.net.Socket;
import java.util.function.Consumer;

public interface TkConn extends Consumer<String> {

  void setAttachment(Object attachment);
  <T> T getAttachment();

  Socket getSocket();
  TkSocketState getState();

  void sendPing() throws IOException;
  void sendPong() throws IOException;

  void close(int code);
  void close(int code, String msg);

}
