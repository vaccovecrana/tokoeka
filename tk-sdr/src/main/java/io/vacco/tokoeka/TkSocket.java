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
      socketConn = new TkConnAdapter(
        socket, socketState,
        (msg) -> send(msg, outputStream),
        (code, msg) -> {
          sendClose(outputStream, code, msg);
          doClose(socket);
        }
      );
      socketHdl.onOpen(socketConn, wsClientHandShakeResponseOf(inputStream));
      return this;
    } catch (Exception e) {
      socketHdl.onError(socketConn, e);
      throw new IllegalStateException("ws connection failed", e);
    }
  }

  public void listen(Supplier<Boolean> go) {
    while (go.get() && !socket.isClosed()) {
      try {
        var stop = handleMessage(socketHdl, socketState, socketConn, inputStream, outputStream);
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
    doClose(this);
  }

  @Override public void accept(String s) {
    send(s, this.outputStream);
  }

  @Override public void close() {
    doClose(socket);
    if (log.isDebugEnabled()) {
      log.debug("ws connection closed");
    }
  }

  public TkSocket withHandler(TkSocketHdl hdl) {
    this.socketHdl = requireNonNull(hdl);
    return this;
  }

  public Socket getSocket() {
    return socket;
  }

  @Override public String toString() {
    return String.format("%s - %s", socket, endpoint);
  }

}
