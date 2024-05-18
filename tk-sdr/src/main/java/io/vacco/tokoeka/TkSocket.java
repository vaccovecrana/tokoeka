package io.vacco.tokoeka;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.*;

public class TkSocket extends WebSocketClient {

  private TkControlHdl handler;

  public TkSocket(URI serverUri) {
    super(serverUri);
  }

  @Override public void onOpen(ServerHandshake hs) {
    if (handler != null) {
      handler.onAuth();
    }
  }

  @Override public void onMessage(ByteBuffer bytes) {
    if (handler != null) {
      handler.accept(bytes);
    }
  }

  @Override public void onMessage(String message) { }

  @Override public void onClose(int code, String reason, boolean remote) {
    if (handler != null && handler.controlPin != null) {
      handler.controlPin.onEvent(code, null, reason, remote, null);
    }
  }

  @Override public void onError(Exception ex) {
    if (handler != null && handler.controlPin != null) {
      handler.controlPin.onEvent(-1, null, null, false, ex);
    }
  }

  public TkSocket withHandler(TkControlHdl handler) {
    this.handler = Objects.requireNonNull(handler);
    return this;
  }

}
