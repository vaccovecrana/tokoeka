package io.vacco.tokoeka.util;

import io.vacco.tokoeka.spi.*;
import org.slf4j.*;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.*;

import static java.nio.ByteBuffer.wrap;
import static java.lang.System.currentTimeMillis;
import static io.vacco.tokoeka.util.TkFormat.shorten;

public class TkSockets {

  public static final int WsCloseTooBig = 1009;
  public static final int WsCloseGoAway = 1001;

  public static final String WsCloseTooBigRes = "msg too big";
  public static final String WsCloseGoAwayRes = "going away";

  public static final int MaxWsHandshakeChars = 2048;
  public static final String magicString = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

  private static final Logger log = LoggerFactory.getLogger(TkSockets.class);

  public static Socket createSocket(String host, int port, boolean secure, int timeout) {
    try {
      if (secure) {
        var sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        var sslSocket = sslSocketFactory.createSocket();
        sslSocket.connect(new InetSocketAddress(host, port), timeout);
        return sslSocket;
      } else {
        var socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), timeout);
        return socket;
      }
    } catch (Exception e) {
      throw new IllegalStateException(String.format(
        "unable to create ws socket: [%s, %d, wss: %s]", host, port, secure
      ), e);
    }
  }

  public static String wsHandShakeOf(String host, int port, String endpoint) {
    var wsKey = Base64.getEncoder().encodeToString(Double.toHexString(Math.random()).getBytes());
    var req =  "GET " + endpoint + " HTTP/1.1\r\n"
      + "Host: " + String.format("%s:%d", host, port) + "\r\n"
      + "Upgrade: websocket\r\n"
      + "Connection: Upgrade\r\n"
      + "Sec-WebSocket-Key: " + wsKey + "\r\n"
      + "Sec-WebSocket-Version: 13\r\n"
      + "\r\n";
    if (log.isTraceEnabled()) {
      log.trace("ws request: {}", req);
    }
    return req;
  }

  public static String wsClientHandShakeResponseOf(Socket sck) throws IOException {
    var reader = new BufferedReader(new InputStreamReader(sck.getInputStream()));
    var bld = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) {
      if (line.isEmpty()) {
        break;
      }
      bld.append(line).append('\n');
    }
    var hs = bld.toString();
    if (!hs.contains("HTTP/1.1 101")) {
      throw new IllegalStateException("ws connection handshake failed: " + hs);
    }
    return hs;
  }

  public static String wsServerHandShakeOf(Socket sck) throws IOException {
    String line;
    var reader = new BufferedReader(new InputStreamReader(sck.getInputStream()));
    var request = new StringBuilder();
    while ((line = reader.readLine()) != null) {
      if (line.isEmpty()) {
        break;
      }
      request.append(line).append("\n");
      if (request.length() >= MaxWsHandshakeChars) {
        throw new IllegalStateException(
          "Incoming connection handshake exceeds max length: " + MaxWsHandshakeChars
        );
      }
    }
    return request.toString();
  }

  public static String performHandshake(Socket sck, String request) throws IOException {
    if (request == null || !request.contains("Upgrade: websocket")) {
      return null;
    }
    var key = request.lines()
      .filter(line -> line.startsWith("Sec-WebSocket-Key:"))
      .map(line -> line.substring(19).trim())
      .findFirst()
      .orElse(null);
    if (key == null) {
      throw new IllegalStateException("Incoming request - missing handshake response: " + request);
    }
    var acceptKey = generateAcceptKey(key);
    var response = "HTTP/1.1 101 Switching Protocols\r\n"
      + "Upgrade: websocket\r\n"
      + "Connection: Upgrade\r\n"
      + "Sec-WebSocket-Accept: " + acceptKey + "\r\n\r\n";
    sendRaw(sck, response.getBytes());
    return response;
  }

  public static byte[] readBlocking(Socket sck, byte[] buff) {
    try {
      var is = sck.getInputStream();
      var bytes = is.read(buff);
      if (log.isTraceEnabled()) {
        log.trace("read {} bytes", bytes);
      }
      if (bytes == -1) {
        throw new IllegalStateException("eof");
      }
      return buff;
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  public static String generateAcceptKey(String key) {
    try {
      var sha1 = MessageDigest.getInstance("SHA-1");
      var hash = sha1.digest((key + magicString).getBytes());
      return Base64.getEncoder().encodeToString(hash);
    } catch (Exception e) {
      throw new IllegalStateException("Error generating accept key", e);
    }
  }

  public static void sendRaw(Socket sck, byte[] data) throws IOException {
    var os = sck.getOutputStream();
    os.write(data);
    os.flush();
  }

  public static void sendPing(Socket sck) {
    try {
      var pingFrame = new byte[] { (byte) 0x89, 0x00 }; // FIN + opcode 0x9 (PING), No payload
      sendRaw(sck, pingFrame);
      log.debug("> PING");
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  public static void sendPong(Socket sck) {
    try {
      var pongFrame = new byte[] { (byte) 0x8A, 0x00 }; // FIN + opcode 0xA (PONG), No payload
      sendRaw(sck, pongFrame);
      log.debug("> PONG");
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  public static void sendClose(Socket sck, int closeCode, String closeReason) {
    try {
      var frame = new ByteArrayOutputStream(); // Close frame header: FIN + opcode 0x8 (CLOSE)
      frame.write(0x88);

      var reasonBytes = closeReason != null ? closeReason.getBytes() : new byte[0];
      int payloadLength = 2 + reasonBytes.length;
      if (payloadLength <= 125) {
        frame.write(payloadLength);
      } else {
        throw new IllegalArgumentException("Close reason too long");
      }

      frame.write((closeCode >> 8) & 0xFF); // most significant byte
      frame.write(closeCode & 0xFF);        // least significant byte
      if (reasonBytes.length > 0) {
        frame.write(reasonBytes);
      }

      sendRaw(sck, frame.toByteArray());
      if (log.isTraceEnabled()) {
        log.trace("> CLOSE: code={}, reason='{}'", closeCode, closeReason);
      }
    } catch (Exception e) {
      log.warn("unable to send close frame - [{}, {}]", closeCode, closeReason, e);
    }
  }

  public static void send(Socket sck, String message) {
    try {
      var payload = message.getBytes();
      var payloadLength = payload.length;
      var frame = new ByteArrayOutputStream();
      frame.write(0x81); // FIN + text frame opcode (0x1)
      if (payloadLength <= 125) {
        frame.write(payloadLength);
      } else if (payloadLength <= 65535) {
        frame.write(126);
        frame.write((payloadLength >> 8) & 0xFF); // most significant byte
        frame.write(payloadLength & 0xFF);        // least significant byte
      } else {
        frame.write(127);  // 8-byte payload length
        // For large payloads (>65535 bytes), write the 8-byte length
        // First four bytes should be zeros (per WebSocket protocol)
        frame.write(0); frame.write(0); frame.write(0); frame.write(0);
        // Write the last four bytes of the payload length
        frame.write((payloadLength >> 24) & 0xFF); // most significant byte
        frame.write((payloadLength >> 16) & 0xFF);
        frame.write((payloadLength >> 8) & 0xFF);
        frame.write(payloadLength & 0xFF);         // least significant byte
      }
      frame.write(payload);
      sendRaw(sck, frame.toByteArray());
      if (log.isTraceEnabled()) {
        log.trace("> TXT: {} ({} bytes)", message, payload.length);
      }
    } catch (Exception e) {
      throw new IllegalStateException(String.format("unable to send text: %s", shorten(message)), e);
    }
  }

  public static void tearDown(Socket socket, TkConn socketConn, TkSocketHdl socketHdl) {
    try {
      if (socketConn != null) {
        var state = socketConn.getState();
        if (state.isClosed()) {
          if (!state.closeByRemote && !socket.isClosed()) {
            sendClose(socket, state.closeCode, state.closeReason);
          }
        }
        socketHdl.onClose(socketConn);
      }
      doClose(socket);
      if (log.isDebugEnabled()) {
        log.debug("ws connection closed - {}, {}", socket, socketConn != null ? socketConn.getState() : "?");
      }
    } catch (Exception e) {
      log.debug("ws connection close error", e);
    }
  }

  public static int payloadLengthOf(Socket sck, byte[] frameHeader) {
    int payloadLength = frameHeader[1] & 0x7F;
    if (payloadLength == 126) {
      var extendedPayloadLength = readBlocking(sck, new byte[2]);
      return wrap(extendedPayloadLength).getShort() & 0xFFFF;
    }
    else if (payloadLength == 127) {
      var extendedPayloadLength = readBlocking(sck, new byte[8]);
      var longPayloadLength = ByteBuffer.wrap(extendedPayloadLength).getLong();
      if (longPayloadLength > Integer.MAX_VALUE) {
        throw new IllegalStateException("payload too large to handle");
      }
      return (int) longPayloadLength;
    }
    return payloadLength;
  }

  public static boolean handleMessage(Socket sck, TkConn conn, TkSocketHdl socketHdl) throws IOException, InterruptedException {
    var socketState = conn.getState();
    if (socketState.isClosed()) {
      return true;
    }

    var frameHeader = readBlocking(sck, new byte[2]);
    var isFinalFragment = (frameHeader[0] & 0x80) != 0; // Check if FIN bit is set
    var opcode = frameHeader[0] & 0x0F;
    var payloadLength = payloadLengthOf(sck, frameHeader);

    if (payloadLength > socketState.maxFrameBytes) {
      log.warn("Frame exceeds maximum allowed size: [{}], closing", payloadLength);
      socketState.markClosed(WsCloseTooBig, WsCloseTooBigRes, false);
      return true;
    }

    var is = sck.getInputStream();
    var payload = new byte[payloadLength];
    int bytesRead = 0;
    while (bytesRead < payloadLength) {
      int read = is.read(payload, bytesRead, payloadLength - bytesRead);
      if (read == -1) {
        throw new IOException("unexpected end of stream");
      }
      bytesRead += read;
    }

    socketState.accumulatedData.write(payload);

    if (isFinalFragment) {
      var completeMessage = socketState.accumulatedData.toByteArray();
      if (opcode == 0x1) {
        var msg = new String(completeMessage);
        if (log.isTraceEnabled()) {
          log.trace("< TXT: {}", shorten(msg));
        }
        socketHdl.onMessage(conn, msg);
      } else if (opcode == 0x2) {
        if (log.isTraceEnabled()) {
          log.trace("< BIN ({})", completeMessage.length);
        }
        socketHdl.onMessage(conn, wrap(completeMessage));
      } else if (opcode == 0xA) {
        log.debug("< PONG");
        socketState.lastPongMs = currentTimeMillis();
        socketHdl.onPong(conn);
      } else if (opcode == 0x9) {
        log.debug("< PING");
        socketHdl.onPing(conn);
      } else if (opcode == 0x8) {
        if (completeMessage.length >= 2) {
          int closeCode = ((completeMessage[0] & 0xFF) << 8) | (completeMessage[1] & 0xFF);
          if (log.isTraceEnabled()) {
            log.trace("< CLOSE ({})", closeCode);
          }
          socketState.markClosed(closeCode, "TODO implement me", true); // TODO implement reading close reason.
        } else {
          log.trace("< CLOSE (?)");
          socketState.markClosed(-1, "remote provided no close code/reason", true);
        }
        return true;
      }
      socketState.accumulatedData.reset();
    }
    return false;
  }

  public static void doClose(Closeable c) {
    try {
      if (c != null) {
        c.close();
      }
    } catch (Exception e) {
      if (log.isWarnEnabled()) {
        log.warn("Error closing {} - {}", c, e.getMessage());
      } else if (log.isDebugEnabled()) {
        log.debug("Error closing {}", c);
      }
    }
  }

}
