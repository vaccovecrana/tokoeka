package io.vacco.tokoeka;

import io.vacco.tokoeka.spi.*;
import io.vacco.tokoeka.util.*;
import org.slf4j.*;
import java.net.Socket;

import static java.util.Objects.requireNonNull;
import static io.vacco.tokoeka.util.TkSockets.*;

public class TkSocket implements TkConn, AutoCloseable {

  private static final Logger log = LoggerFactory.getLogger(TkSocket.class);

  private final String        host;
  private final int           port;
  private final boolean       secure;
  private final int           timeout;
  private final String        endpoint;
  private final TkSocketState socketState;

  private Socket        socket;
  private TkSocketHdl   socketHdl;
  private TkConn        socketConn;

  public TkSocket(String host, int port, String endpoint, boolean secure, int timeout, TkSocketState socketState) {
    this.host = requireNonNull(host);
    this.port = port;
    this.secure = secure;
    this.timeout = timeout;
    this.endpoint = requireNonNull(endpoint);
    this.socketState = requireNonNull(socketState);
  }

  public TkSocket connect() {
    try {
      socket = createSocket(host, port, secure, timeout);
      sendRaw(socket, wsHandShakeOf(host, port, endpoint).getBytes());
      socketConn = new TkSocketConn(socket, socketState, socketHdl);
      socketHdl.onOpen(socketConn, wsClientHandShakeResponseOf(socket));
      return this;
    } catch (Exception e) {
      socketHdl.onError(socketConn, e);
      tearDown(socket, socketConn, socketHdl);
      throw new IllegalStateException("ws connection failed", e);
    }
  }

  public void listen() {
    try {
      while (!socket.isClosed()) {
        var stop = handleMessage(socket, socketConn, socketHdl);
        if (stop) {
          break;
        }
      }
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("ws message processing error", e);
      }
      socketHdl.onError(socketConn, e);
    } finally {
      tearDown(socket, socketConn, socketHdl);
    }
  }

  @Override public void accept(String s) {
    send(this.socket, s);
  }

  public TkSocket withHandler(TkSocketHdl hdl) {
    this.socketHdl = requireNonNull(hdl);
    return this;
  }

  @Override public void setAttachment(Object attachment) {
    this.socketState.attachment = requireNonNull(attachment);
  }

  @SuppressWarnings("unchecked")
  @Override public <T> T getAttachment() {
    return (T) this.socketState.attachment;
  }

  @Override public Socket getSocket() {
    return this.socket;
  }

  @Override public TkSocketState getState() {
    return this.socketState;
  }

  @Override public void sendPing() {
    this.socketConn.sendPing();
  }

  @Override public void sendPong() {
    this.socketConn.sendPong();
  }

  @Override public void close(int code) {
    this.socketConn.close(code);
  }

  @Override public void close(int code, String msg) {
    this.socketConn.close(code, msg);
  }

  @Override public void close() {
    this.socketConn.close(this.socketState.closeCode);
  }

  @Override public String toString() {
    return String.format("%s - %s", socket, endpoint);
  }

}
