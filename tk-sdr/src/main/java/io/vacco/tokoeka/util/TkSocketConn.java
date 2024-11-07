package io.vacco.tokoeka.util;

import io.vacco.tokoeka.spi.*;
import java.net.Socket;

import static java.util.Objects.requireNonNull;
import static io.vacco.tokoeka.util.TkSockets.*;

public class TkSocketConn implements TkConn {

  private final Socket        socket;
  private final TkSocketState socketState;
  private final TkSocketHdl   socketHdl;

  public TkSocketConn(Socket socket, TkSocketState socketState, TkSocketHdl socketHdl) {
    this.socket = requireNonNull(socket);
    this.socketState = requireNonNull(socketState);
    this.socketHdl = requireNonNull(socketHdl);
  }

  @Override public void setAttachment(Object attachment) {
    socketState.attachment = requireNonNull(attachment);
  }

  @SuppressWarnings("unchecked")
  @Override public <T> T getAttachment() {
    return (T) socketState.attachment;
  }

  @Override public void accept(String s) {
    send(socket, s);
  }

  @Override public Socket getSocket() {
    return socket;
  }

  @Override public TkSocketState getState() {
    return socketState;
  }

  @Override public void close(int code) {
    socketState.markClosed(code, null, false);
    tearDown(socket, this, socketHdl);
  }

  @Override public void close(int code, String msg) {
    socketState.markClosed(code, msg, false);
    tearDown(socket, this, socketHdl);
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
