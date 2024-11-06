package io.vacco.tokoeka.handler;

import io.vacco.tokoeka.audio.TkAdpcm;
import io.vacco.tokoeka.schema.TkConfig;
import io.vacco.tokoeka.spi.*;
import io.vacco.tokoeka.util.*;
import org.slf4j.*;
import java.nio.*;
import java.util.Objects;
import java.util.function.Consumer;

import static io.vacco.tokoeka.audio.TkAudio.*;
import static io.vacco.tokoeka.util.TkCommand.*;

public class TkAudioHdl {

  private static final Logger log = LoggerFactory.getLogger(TkAudioHdl.class);

  private final TkAdpcm           adPcm;
  private final TkConfig          config;
  private final TkAudioPin        audioPin;
  private final TkTimer<Void>     timer;
  private final Consumer<String>  tx;

  public TkAudioHdl(TkConfig config, Consumer<String> tx, TkAudioPin audioPin) {
    this.adPcm = new TkAdpcm();
    this.config = Objects.requireNonNull(config);
    this.audioPin = Objects.requireNonNull(audioPin);
    this.tx = Objects.requireNonNull(tx);
    this.timer = new TkTimer<>(3000, (Nil) -> tx.accept(setKeepAlive()));
  }

  public void updateAudioParams() {
    tx.accept(setLittleEndian());
    tx.accept(setGenAttn(0));
    tx.accept(setGenMix(0, -1)); // TODO map to config
    tx.accept(setModulation(
        config.modulation,
        removeFreqOffset(config.frequencyKHz, config.frequencyOffset, config.frequencyMax))
    );
    tx.accept(setAGC(
        config.agcOn, config.agcHang, config.agcThresh,
        config.agcSlope, config.agcDecay, config.agcGain
    ));
    tx.accept(setCompression(config.compression));

    switch (config.nrAlgoId) {
      case 3:
        tx.accept(setNrAlgo(config.nrAlgoId));
        setMulti(tx, setNrAlgoSpecParams(config.nrSpecGain, config.nrSpecAlpha, config.nrSpecActiveSnr));
        break;
      default: log.warn("Unknown/Unsupported NR algo ID: {}", config.nrAlgoId);
    }

    tx.accept(setSquelch(config.squelchThresholdDb, config.squelchTailLength));
  }

  public void onAudioRate(int ar) {
    tx.accept(setAudioRate(ar, config.sampleRateOut));
  }

  public void onSampleRate(double sr) {
    this.config.sampleRate = sr;
    this.updateAudioParams();
    tx.accept(setKeepAlive());
  }

  public void processAudio(TkConn conn, ByteBuffer buffer) {
    if (buffer.remaining() < 7) {
      log.warn("Received audio data is too short");
      return;
    }

    buffer.order(ByteOrder.LITTLE_ENDIAN);
    var flags = buffer.get() & 0xFF;
    var sequenceNumber = buffer.getInt();

    buffer.order(ByteOrder.BIG_ENDIAN);
    var sMeter = buffer.getShort() & 0xFFFF;
    var rssi = 0.1 * sMeter - 127.0;

    if (buffer.hasRemaining()) {
      buffer.order(ByteOrder.LITTLE_ENDIAN);
      byte[] audio = new byte[buffer.remaining()], imaPcm = null, rawPcm;
      buffer.get(audio);
      if (config.compression) {
        imaPcm = audio;
        rawPcm = adPcm.decode(audio);
      } else {
        rawPcm = audio;
      }
      audioPin.onAudio(conn, (int) config.sampleRate, flags, sequenceNumber, sMeter, rssi, imaPcm, rawPcm);
    }

    timer.update(null);
  }

}
