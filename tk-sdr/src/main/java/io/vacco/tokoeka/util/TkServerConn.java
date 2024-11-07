package io.vacco.tokoeka.util;

import io.vacco.tokoeka.spi.TkConn;
import java.net.Socket;
import java.util.Objects;
import java.util.function.Consumer;

public class TkServerConn implements TkConn {

  private final Socket socket;
  private final TkSocketState socketState;
  private final Consumer<String> tx;

  public TkServerConn(Socket socket, TkSocketState socketState, Consumer<String> tx) {
    this.socket = Objects.requireNonNull(socket);
    this.socketState = Objects.requireNonNull(socketState);
    this.tx = Objects.requireNonNull(tx);
  }

  @Override public void setAttachment(Object attachment) {
    socketState.attachment = Objects.requireNonNull(attachment);
  }

  @SuppressWarnings("unchecked")
  @Override public <T> T getAttachment() {
    return (T) socketState.attachment;
  }

  @Override public void accept(String s) {
    tx.accept(s);
  }

  @Override public Socket getSocket() {
    return socket;
  }

  @Override public TkSocketState getState() {
    return socketState;
  }

  @Override public void close(int code) {
    socketState.markClosed(code, null, false);
  }

  @Override public void close(int code, String msg) {
    socketState.markClosed(code, msg, false);
  }

  @Override public void sendPing() {
    TkSockets.sendPing(socket);
  }

  @Override public void sendPong() {
    TkSockets.sendPong(socket);
  }

  @Override public String toString() {
    return String.format("%s, %s", socket, socketState);
  }

}
