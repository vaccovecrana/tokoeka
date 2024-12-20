package io.vacco.tokoeka;

import io.vacco.tokoeka.spi.*;
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
  private final ExecutorService         clientPool;

  private ServerSocket serverSocket;

  public TkSocketServer(int port, TkSocketHdl socketHdl, Supplier<TkSocketState> stateFn, ExecutorService clientPool) {
    this.port = port;
    this.stateFn = requireNonNull(stateFn);
    this.socketHdl = requireNonNull(socketHdl);
    this.clientPool = requireNonNull(clientPool);
  }

  public void start() {
    try {
      serverSocket = new ServerSocket(port);
      log.info("WebSocket server started on port {}", port);
      while (!serverSocket.isClosed()) {
        var clientSocket = serverSocket.accept();
        clientPool.submit(() -> handleClient(clientSocket));
      }
    } catch (IOException e) {
      throw new IllegalStateException("Unable to start websocket server", e);
    }
  }

  private void handleClient(Socket clientSocket) {
    log.debug("Client connection: {}", clientSocket);
    TkConn conn = null;
    try {
      var socketState = this.stateFn.get();
      var handShake = wsServerHandShakeOf(clientSocket);
      var handshakeResponse = performHandshake(clientSocket, handShake);
      conn = new TkSocketConn(clientSocket, socketState, this.socketHdl);
      this.socketHdl.onOpen(conn, handshakeResponse);
      while (!clientSocket.isClosed()) {
        var stop = handleMessage(clientSocket, conn, this.socketHdl);
        if (stop) {
          break;
        }
      }
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("Client connection error - {}", clientSocket.getRemoteSocketAddress(), e);
      }
    } finally {
      tearDown(clientSocket, conn, socketHdl);
    }
  }

  @Override public void close() {
    clientPool.shutdown();
    if (serverSocket != null) {
      tryClose(serverSocket);
    }
  }

}
