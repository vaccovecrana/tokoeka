package io.vacco.tokoeka;

import io.vacco.tokoeka.spi.TkSocketHdl;
import org.slf4j.*;
import java.io.*;
import java.net.Socket;
import java.util.function.*;

import static java.nio.ByteBuffer.wrap;
import static java.util.Objects.requireNonNull;
import static io.vacco.tokoeka.util.TkSockets.*;

public class TkSocket implements AutoCloseable, Consumer<String> {

  private static final Logger log = LoggerFactory.getLogger(TkSocket.class);

  private final String  endpoint;
  private final Socket  socket;

  private OutputStream  outputStream;
  private InputStream   inputStream;
  private TkSocketHdl   socketHdl;

  private final ByteArrayOutputStream accumulatedData = new ByteArrayOutputStream();

  public TkSocket(String host, int port, String endpoint, boolean secure, int timeout) {
    this.endpoint = requireNonNull(endpoint);
    this.socket = createSocket(host, port, secure, timeout);
  }

  public TkSocket connect() {
    try {
      outputStream = socket.getOutputStream();
      inputStream = socket.getInputStream();
      outputStream.write(wsHandShakeOf(endpoint).getBytes());
      outputStream.flush();
      var reader = new BufferedReader(new InputStreamReader(inputStream));
      var bld = new StringBuilder();
      String line;
      while (!(line = reader.readLine()).isEmpty()) {
        bld.append(line).append('\n');
      }
      var hs = bld.toString();
      if (!hs.contains("HTTP/1.1 101")) {
        throw new IllegalStateException("ws connection handshake failed: " + hs);
      }
      this.socketHdl.onOpen(hs);
      return this;
    } catch (Exception e) {
      this.socketHdl.onError(e);
      throw new IllegalStateException("ws connection open failed", e);
    }
  }

  private void sendPong() throws IOException {
    var pongFrame = new byte[2];
    pongFrame[0] = (byte) 0x8A; // 0x8A = FIN + opcode 0xA (PONG)
    outputStream.write(pongFrame);
    outputStream.flush();
    if (log.isTraceEnabled()) {
      log.trace("< PONG");
    }
  }

  public void send(String message) {
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
      outputStream.write(frame.toByteArray());
      outputStream.flush();
      if (log.isDebugEnabled()) {
        log.debug("< TXT: {} ({} bytes)", message, payload.length);
      }
    } catch (Exception e) {
      throw new IllegalStateException(String.format("unable to send text: %s", message), e);
    }
  }

  public void listen(Supplier<Boolean> go) {
    while (go.get() && !socket.isClosed()) {
      try {
        var frameHeader = new byte[2];
        read(inputStream, frameHeader);
        var isFinalFragment = (frameHeader[0] & 0x80) != 0; // Check if FIN bit is set
        var opcode = frameHeader[0] & 0x0F;
        var payloadLength = payloadLengthOf(frameHeader, inputStream);
        var payload = new byte[payloadLength];
        int bytesRead = 0;
        while (bytesRead < payloadLength) {
          int read = inputStream.read(payload, bytesRead, payloadLength - bytesRead);
          if (read == -1) {
            throw new IOException("unexpected end of stream");
          }
          bytesRead += read;
        }
        accumulatedData.write(payload);
        if (isFinalFragment) {
          var completeMessage = accumulatedData.toByteArray();
          if (opcode == 0x1) {
            var msg = new String(completeMessage);
            if (log.isTraceEnabled()) {
              log.trace("> TXT: {}", msg);
            }
            this.socketHdl.onMessage(msg);
          } else if (opcode == 0x2) {
            if (log.isTraceEnabled()) {
              log.trace("> BIN ({})", completeMessage.length);
            }
            this.socketHdl.onMessage(wrap(completeMessage));
          } else if (opcode == 0xA) {
            if (log.isTraceEnabled()) {
              log.trace("> PONG");
            }
          } else if (opcode == 0x9) {
            if (log.isTraceEnabled()) {
              log.trace("> PING");
            }
            sendPong();
          } else if (opcode == 0x8) {
            if (completeMessage.length >= 2) {
              int closeCode = ((completeMessage[0] & 0xFF) << 8) | (completeMessage[1] & 0xFF);
              if (log.isTraceEnabled()) {
                log.trace("> CLOSE ({})", closeCode);
              }
              this.socketHdl.onClose(closeCode);
            } else {
              log.trace("> CLOSE (?)");
              throw new IllegalStateException("Received close frame with no close code.");
            }
            break;
          }
          accumulatedData.reset();
        }
      } catch (Exception e) {
        if (log.isDebugEnabled()) {
          log.debug("ws message processing error", e);
        }
        this.socketHdl.onError(e);
        break;
      }
    }
    this.close();
  }

  @Override public void accept(String s) {
    this.send(s);
  }

  @Override public void close() {
    try {
      socket.close();
    } catch (IOException e) {
      log.error("Unable to close ws socket: {} - {}", this, e.getMessage());
    }
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
