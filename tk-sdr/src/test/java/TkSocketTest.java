import io.vacco.shax.logging.ShOption;
import io.vacco.tokoeka.*;
import io.vacco.tokoeka.schema.TkModulation;
import io.vacco.tokoeka.schema.TkConfig;
import io.vacco.tokoeka.util.TkAudioPlayer;
import io.vacco.tokoeka.util.TkSquelch;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static j8spec.J8Spec.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class TkSocketTest {

  static {
    ShOption.setSysProp(ShOption.IO_VACCO_SHAX_DEVMODE, "true");
    ShOption.setSysProp(ShOption.IO_VACCO_SHAX_LOGLEVEL, "info");
  }

  private static final Logger log = LoggerFactory.getLogger(TkSocketTest.class);

  static {
    it("Can communicate with a KiwiSDR server", () -> {
      if (!GraphicsEnvironment.isHeadless()) {
        // sdr.vebik.cz:8073 - 639kHz
        // 85.191.35.22:8073
        // sdr.hfunderground.com:8074 - 880kHz good quality!
        // 80m.live:8078
        // hb9bxewebsdr.ddns.net:8073
        // bclinfo.ddns.net:8073 - 13750kHz, 594kHz, 1600kHz
        var uri = new URI("ws://173.48.189.54:8074/12287283/SND"); // This looks like a session ID
        var cfg = new TkConfig();
        var sock = new TkSocket(uri);
        var send = (Consumer<String>) (s) -> {
          if (sock.isOpen()) {
            log.info(s);
            sock.send(s);
          }
        };

        var squelch = new TkSquelch(0, (audio, signalAvg) -> {
          log.info(">>>> Squelch. Signal avg: {}", signalAvg);
        }, 1.0);

        TkAudioPlayer[] player = new TkAudioPlayer[1];

        var ctlHdl = new TkControlHdl(cfg, send)
            .withAudioHandler(new TkAudioHdl(cfg, send, (flags, sequenceNumber, sMeter, rssi, rawData) -> {
              log.info("flags: {} seqNo: {} sMeter: {} rssi: {} raw: {}", flags, sequenceNumber, sMeter, String.format("%6.2f", rssi), rawData.length);
              // log.info("squelch threshold: {}", squelch.threshold);
              squelch.processAudio(rawData);
              if (player[0] == null) {
                player[0] = new TkAudioPlayer((int) cfg.sampleRate, 16, 1);
              }
              player[0].play(rawData);
            }));
        var latch = new CountDownLatch(1);

        sock.withHandler(ctlHdl)
            .withPin((code, reason, remote, e) -> latch.countDown());

        squelch.detectNoiseFloor(4500, 2);

        cfg.username = "kiwi";
        cfg.identUser = "tokoeka";
        cfg.modulation = TkModulation.usb;
        cfg.frequency = 14265;
        cfg.compression = true;

        cfg.agcOn = false;
        cfg.agcHang = false;
        cfg.agcThresh = -100;
        cfg.agcSlope = 6;
        cfg.agcDecay = 1000;
        cfg.agcGain = 65;

        cfg.nrAlgoId = 3;
        cfg.nrSpecAlpha = 0.95;
        cfg.nrSpecGain = 1;
        cfg.nrSpecActiveSnr = 1000;

        sock.connectBlocking();
        log.info("{}", latch.await(90, TimeUnit.SECONDS));
        sock.closeBlocking();
        player[0].close();
      } else {
        log.info("CI/CD build. Stopping.");
      }
    });
  }
}
