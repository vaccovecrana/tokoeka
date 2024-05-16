import com.google.gson.Gson;
import io.vacco.shax.logging.ShOption;
import io.vacco.tokoeka.*;
import io.vacco.tokoeka.config.TkModulation;
import io.vacco.tokoeka.config.TkConfig;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;
import org.slf4j.*;

import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Consumer;

import static j8spec.J8Spec.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class TkControlHdlTest {

  private static final Gson g = new Gson();

  static {
    ShOption.setSysProp(ShOption.IO_VACCO_SHAX_DEVMODE, "true");
    ShOption.setSysProp(ShOption.IO_VACCO_SHAX_LOGLEVEL, "debug");
  }

  private static final Logger log = LoggerFactory.getLogger(TkSocketTest.class);

  static {
    it("Replays Websocket audio messages", () -> {
      var sndUrl = Objects.requireNonNull(TkControlHdlTest.class.getResource("/sdr-snd-log.json"));
      var wfUrl = Objects.requireNonNull(TkControlHdlTest.class.getResource("/sdr-wf-log.json"));
      var sndMessages = g.fromJson(new InputStreamReader(sndUrl.openStream()), WsMsg[].class);
      var wfMessages = g.fromJson(new InputStreamReader(wfUrl.openStream()), WsMsg[].class);

      var cfg = new TkConfig();
      cfg.modulation = TkModulation.am;

      var send = (Consumer<String>) log::info;
      var ctlHdl = new TkControlHdl(cfg, send)
          .withAudioHandler(new TkAudioHdl(cfg, send, (flags, sequenceNumber, sMeter, rssi, rawData) -> {
            log.info("flags: {} seqNo: {} sMeter: {} rssi: {} raw: {}", flags, sequenceNumber, sMeter, rssi, rawData.length);
          }))
          .withWaterfallHandler(new TkWaterfallHdl(send, (xBin, sequenceNumber, flags, rawWfData) -> {
            log.info("bin: {} seqNo: {} flags: {} wfData: {}", xBin, sequenceNumber, flags, rawWfData.length);
          }))
          .withJsonIn(g::fromJson);

      for (var msg : sndMessages) {
        if ("receive".equals(msg.type)) {
          var bytes = ByteBuffer.wrap(Base64.getDecoder().decode(msg.data));
          ctlHdl.accept(bytes);
        } else {
          log.info("Ref command ==> {}", msg.data);
        }
      }
      for (var msg : wfMessages) {
        if ("receive".equals(msg.type)) {
          var bytes = ByteBuffer.wrap(Base64.getDecoder().decode(msg.data));
          ctlHdl.accept(bytes);
        }
      }

      log.info("Finished");
    });
  }
}
