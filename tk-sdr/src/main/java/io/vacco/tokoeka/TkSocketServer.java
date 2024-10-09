package io.vacco.tokoeka;

import io.vacco.tokoeka.spi.TkSocketHdl;
import io.vacco.tokoeka.util.*;
import org.slf4j.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.function.*;

import static java.util.Objects.requireNonNull;
import static io.vacco.tokoeka.util.TkSockets.*;

public class TkSocketServer implements Closeable {

  private static final Logger log = LoggerFactory.getLogger(TkSocketServer.class);

  private final int                     port;
  private final TkSocketHdl             socketHdl;
  private final Supplier<TkSocketState> stateFn;
  private final ExecutorService         clientThreadPool = Executors.newCachedThreadPool();

  private ServerSocket serverSocket;

  public TkSocketServer(int port, TkSocketHdl socketHdl, Supplier<TkSocketState> stateFn) {
    this.port = port;
    this.stateFn = requireNonNull(stateFn);
    this.socketHdl = requireNonNull(socketHdl);
  }

  public void start() {
    try {
      serverSocket = new ServerSocket(port);
      log.info("WebSocket server started on port {}", port);
      while (!serverSocket.isClosed()) {
        var clientSocket = serverSocket.accept();
        clientThreadPool.submit(() -> handleClient(clientSocket));
      }
    } catch (IOException e) {
      throw new IllegalStateException("Unable to start websocket server", e);
    }
  }

  private void handleClient(Socket clientSocket) {
    log.debug("Incoming connection: {}", clientSocket);
    try {
      var inputStream = clientSocket.getInputStream();
      var outputStream = clientSocket.getOutputStream();
      var socketState = this.stateFn.get();
      var handShake = wsServerHandShakeOf(inputStream);
      var handshakeResponse = performHandshake(handShake, outputStream);
      if (handshakeResponse != null) {
        var conn = new TkConnAdapter(
          clientSocket, socketState,
          msg -> send(msg, outputStream),
          (code, msg) -> {
            sendClose(outputStream, code, msg);
            doClose(clientSocket);
          }
        );
        this.socketHdl.onOpen(conn, handshakeResponse);
        while (!clientSocket.isClosed()) {
          var stop = handleMessage(this.socketHdl, socketState, conn, inputStream, outputStream);
          if (stop) {
            break;
          }
        }
      } else {
        throw new IllegalStateException("Incoming connection - missing handshake response " + clientSocket);
      }
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("Incoming connection handler error - {}", clientSocket.getRemoteSocketAddress(), e);
      }
    } finally {
      doClose(clientSocket);
    }
  }

  @Override public void close() {
    clientThreadPool.shutdown();
    if (serverSocket != null) {
      doClose(serverSocket);
    }
  }

}
