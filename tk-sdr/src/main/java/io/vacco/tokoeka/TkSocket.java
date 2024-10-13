package io.vacco.tokoeka;

import io.vacco.tokoeka.spi.*;
import io.vacco.tokoeka.util.*;
import org.slf4j.*;
import java.io.*;
import java.net.Socket;
import java.util.function.*;

import static java.util.Objects.requireNonNull;
import static io.vacco.tokoeka.util.TkSockets.*;

public class TkSocket implements Closeable, Consumer<String> {

  private static final Logger log = LoggerFactory.getLogger(TkSocket.class);

  private final String        host;
  private final int           port;
  private final boolean       secure;
  private final int           timeout;
  private final String        endpoint;
  private final TkSocketState socketState;

  private Socket        socket;
  private OutputStream  outputStream;
  private InputStream   inputStream;
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
      outputStream = socket.getOutputStream();
      inputStream = socket.getInputStream();
      outputStream.write(wsHandShakeOf(host, port, endpoint).getBytes());
      outputStream.flush();
      socketConn = new TkConnAdapter(socket, socketState, (msg) -> send(msg, outputStream));
      socketHdl.onOpen(socketConn, wsClientHandShakeResponseOf(inputStream));
      return this;
    } catch (Exception e) {
      socketHdl.onError(socketConn, e);
      doClose(this);
      throw new IllegalStateException("ws connection failed", e);
    }
  }

  public void listen() {
    while (!socket.isClosed()) {
      try {
        var stop = handleMessage(socketHdl, socketConn, inputStream, outputStream);
        if (stop) {
          break;
        }
      } catch (Exception e) {
        if (log.isDebugEnabled()) {
          log.debug("ws message processing error", e);
        }
        socketHdl.onError(socketConn, e);
        break;
      }
    }
    tearDown(socket, socketConn, socketHdl);
  }

  @Override public void accept(String s) {
    send(s, this.outputStream);
  }

  @Override public void close() {
    tearDown(socket, socketConn, socketHdl);
  }

  public TkSocket withHandler(TkSocketHdl hdl) {
    this.socketHdl = requireNonNull(hdl);
    return this;
  }

  @Override public String toString() {
    return String.format("%s - %s", socket, endpoint);
  }

}
