package io.vacco.tokoeka.util;

import io.vacco.tokoeka.spi.TkConn;
import java.net.Socket;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TkConnAdapter implements TkConn {

  private final Socket socket;
  private final TkSocketState socketState;
  private final Consumer<String> tx;
  private final BiConsumer<Integer, String> onClose;

  public TkConnAdapter(Socket socket, TkSocketState socketState,
                       Consumer<String> tx, BiConsumer<Integer, String> onClose) {
    this.socket = Objects.requireNonNull(socket);
    this.socketState = Objects.requireNonNull(socketState);
    this.tx = Objects.requireNonNull(tx);
    this.onClose = Objects.requireNonNull(onClose);
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

  @Override public void close(int code) {
    onClose.accept(code, null);
  }

  @Override public void close(int code, String msg) {
    onClose.accept(code, msg);
  }

  @Override public String toString() {
    return socket.toString();
  }

}
