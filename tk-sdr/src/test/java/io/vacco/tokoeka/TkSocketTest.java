package io.vacco.tokoeka;

import io.vacco.tokoeka.audio.*;
import io.vacco.tokoeka.handler.*;
import io.vacco.tokoeka.schema.*;
import io.vacco.tokoeka.util.TkSocketState;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;
import org.slf4j.*;

import static j8spec.J8Spec.*;
import static io.vacco.tokoeka.util.TkCounter.nowMsDiffLt;
import static io.vacco.tokoeka.TkLogging.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class TkSocketTest {

  static { initLog(); }

  private static final Logger log = LoggerFactory.getLogger(TkSocketTest.class);

  static {
    it("Can communicate with a KiwiSDR server", localTest(() -> {
      // sdr.vebik.cz:8073 - 639kHz
      // 85.191.35.22:8073
      // sdr.hfunderground.com:8074 - 880kHz good quality!
      // 80m.live:8078
      // hb9bxewebsdr.ddns.net:8073
      // bclinfo.ddns.net:8073 - 13750kHz, 594kHz, 1600kHz

      var state = TkSocketState.of(-1, 65536);
      var sock = new TkSocket("sdr.hfunderground.com", 8074, "/12287283/SND", false, 3000, state);
      var cfg = new TkConfig();
      var sqParams = TkSquelchParams.of(2500, 4.0);
      var squelch = new TkSquelch(sqParams)
        .withPin((open, pcm, signalAvg, signalThr) -> log.info(">>>> Squelch [open: {}, avg: {}, thr: {}]", open, signalAvg, signalThr));
      var player = new TkAudioPlayer(16, 1);

      var go = new boolean[] { true };
      var nowMs = System.currentTimeMillis();

      var ctlHdl = new TkControlHdl(cfg)
        .withAudioHandler(new TkAudioHdl(cfg, sock, (sampleRate, flags, sequenceNumber, sMeter, rssi, imaPcm, rawPcm) -> {
          log.info("flags: {} seqNo: {} sMeter: {} rssi: {} raw: {}", flags, sequenceNumber, sMeter, String.format("%6.2f", rssi), rawPcm.length);
          squelch.processAudio(rawPcm);
          player.play(sampleRate, rawPcm);
        }))
        .withControlPin((code, key, value, remote, e) -> {
          log.info("control event: {} [{}] [{}] {}", code, key, value, remote, e);
          var isError = code > 1000
            || (value != null && value.equals("Operation timed out"))
            || (code == -1 && key == null && value == null && !remote);
          if (isError) {
            go[0] = false;
          }
        });

      sock.withHandler(ctlHdl);

      cfg.username = "kiwi";
      cfg.identUser = "tokoeka";
      cfg.modulation = TkModulation.usb;
      cfg.frequencyKHz = 13354;
      cfg.compression = true;

      cfg.agcOn = false;
      cfg.agcHang = false;
      cfg.agcThresh = -100;
      cfg.agcSlope = 6;
      cfg.agcDecay = 1000;
      cfg.agcGain = 70;

      cfg.nrAlgoId = 3;
      cfg.nrSpecAlpha = 0.95;
      cfg.nrSpecGain = 1;
      cfg.nrSpecActiveSnr = 1000;

      sock.connect().listen(() -> go[0] && nowMsDiffLt(nowMs, 120_000));
      player.close();
    }));
  }
}
