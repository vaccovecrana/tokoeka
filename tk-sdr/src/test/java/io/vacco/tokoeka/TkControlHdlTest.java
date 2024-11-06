package io.vacco.tokoeka;

import com.google.gson.Gson;
import io.vacco.tokoeka.handler.*;
import io.vacco.tokoeka.schema.*;
import io.vacco.tokoeka.spi.TkConn;
import io.vacco.tokoeka.util.TkSocketState;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;
import org.slf4j.*;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.*;

import static j8spec.J8Spec.*;
import static io.vacco.tokoeka.TkLogging.initLog;
import static io.vacco.tokoeka.util.TkFormat.shorten;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class TkControlHdlTest {

  static {
    initLog();
  }

  private static final Gson g = new Gson();
  private static final Logger log = LoggerFactory.getLogger(TkControlHdlTest.class);

  static {
    it("Replays Websocket audio messages", () -> {
      var sndUrl = Objects.requireNonNull(TkControlHdlTest.class.getResource("/sdr-snd-log.json"));
      var wfUrl = Objects.requireNonNull(TkControlHdlTest.class.getResource("/sdr-wf-log.json"));
      var sndMessages = g.fromJson(new InputStreamReader(sndUrl.openStream()), WsMsg[].class);
      var wfMessages = g.fromJson(new InputStreamReader(wfUrl.openStream()), WsMsg[].class);

      var cfg = new TkConfig();
      cfg.modulation = TkModulation.am;

      var state = TkSocketState.of(-1, 65536);
      var conn = new TkConn() {
        @Override public void accept(String s) { log.info(s); }
        @Override public void setAttachment(Object attachment) {}
        @Override public <T> T getAttachment() { return null; }
        @Override public Socket getSocket() { return null; }
        @Override public TkSocketState getState() { return state; }
        @Override public void sendPing() { log.info("I send the pingz"); }
        @Override public void sendPong() { log.info("I send the pongz"); }
        @Override public void close(int code) { log.info("close - [{}]", code); }
        @Override public void close(int code, String msg) {
          log.info("close - [{}, {}]", code, msg);
        }
      };

      var ctlHdl = new TkControlHdl(cfg)
        .withAudioHandler(new TkAudioHdl(cfg, conn, (conn0, sampleRate, flags, sequenceNumber, sMeter, rssi, imaPcm, rawPcm) -> {
          log.info("flags: {} seqNo: {} sMeter: {} rssi: {} raw: {}", flags, sequenceNumber, sMeter, rssi, rawPcm.length);
        }))
        .withWaterfallHandler(new TkWaterfallHdl(conn, (xBin, sequenceNumber, flags, rawWfData) -> {
          log.info("bin: {} seqNo: {} flags: {} wfData: {}", xBin, sequenceNumber, flags, rawWfData.length);
        }))
        .withJsonIn(g::fromJson)
        .withConfigPin(((kiwiConfig, dxConfig, dxCommConfig) -> {
          for (var o : new Object[]{kiwiConfig, dxConfig, dxCommConfig}) {
            if (o != null) {
              log.info(shorten(g.toJson(o)));
            }
          }
        }));

      for (var msg : sndMessages) {
        if ("receive".equals(msg.type)) {
          var bytes = ByteBuffer.wrap(Base64.getDecoder().decode(msg.data));
          ctlHdl.onMessage(conn, bytes);
        } else {
          log.info("Ref command ==> {}", shorten(msg.data));
        }
      }

      for (var msg : wfMessages) {
        if ("receive".equals(msg.type)) {
          var bytes = ByteBuffer.wrap(Base64.getDecoder().decode(msg.data));
          ctlHdl.onMessage(conn, bytes);
        }
      }

      log.info("Finished");
    });
  }
}
