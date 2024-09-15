package io.vacco.tokoeka.util;

import org.slf4j.*;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Base64;

import static java.nio.ByteBuffer.wrap;

public class TkSockets {

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

  public static void read(InputStream is, byte[] buff) {
    try {
      var bytes = is.read(buff);
      if (log.isTraceEnabled()) {
        log.trace("read {} bytes", bytes);
      }
      if (bytes == -1) {
        throw new IllegalStateException("eof");
      }
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  public static int payloadLengthOf(byte[] frameHeader, InputStream is) {
    int payloadLength = frameHeader[1] & 0x7F;
    if (payloadLength == 126) {
      var extendedPayloadLength = new byte[2];
      read(is, extendedPayloadLength);
      return wrap(extendedPayloadLength).getShort() & 0xFFFF;
    }
    else if (payloadLength == 127) {
      var extendedPayloadLength = new byte[8];
      read(is, extendedPayloadLength);
      var longPayloadLength = ByteBuffer.wrap(extendedPayloadLength).getLong();
      if (longPayloadLength > Integer.MAX_VALUE) {
        throw new IllegalStateException("payload too large to handle");
      }
      return (int) longPayloadLength;
    }
    return payloadLength;
  }

}
