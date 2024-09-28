package io.vacco.tokoeka.audio;

import io.vacco.tokoeka.schema.TkSquelchParams;
import io.vacco.tokoeka.spi.TkSquelchPin;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import static io.vacco.tokoeka.audio.TkAudio.signalAverageOf;

public class TkSquelch {

  private final TkSquelchParams params;

  private double threshold;
  private TkSquelchPin pin;
  private long lastSignalTime = 0;
  private boolean squelchOpen = false;
  private final Map<Integer, Long> noiseHistogram = new HashMap<>();

  public TkSquelch(TkSquelchParams params) {
    this.params = Objects.requireNonNull(params);
  }

  public void processAudio(byte[] pcm) {
    var currentTime = System.currentTimeMillis();
    var signalAvg = signalAverageOf(pcm);

    if (signalAvg > threshold) {
      lastSignalTime = currentTime;
      squelchOpen = true;
    } else if ((currentTime - lastSignalTime) > params.tailTimeMs) {
      if (squelchOpen && pin != null) {
        pin.onSquelch(false, pcm, signalAvg, threshold);
      }
      squelchOpen = false;
    }
    if (squelchOpen && pin != null) {
      pin.onSquelch(true, pcm, signalAvg, threshold);
    }

    updateNoiseFloor(signalAvg);
  }

  private void updateNoiseFloor(double signalAvg) {
    int signalBin = (int) signalAvg;
    if (signalBin == 0) {
      signalBin = 1;
    }
    noiseHistogram.put(signalBin, noiseHistogram.getOrDefault(signalBin, 0L) + 1);
    int mostFrequentBin = signalBin;
    long maxCount = 0;
    for (var e : noiseHistogram.entrySet()) {
      if (e.getValue() > maxCount) {
        maxCount = e.getValue();
        mostFrequentBin = e.getKey();
      }
    }
    threshold = mostFrequentBin * params.nfMultiplier;
  }

  public TkSquelch withPin(TkSquelchPin pin) {
    this.pin = Objects.requireNonNull(pin);
    return this;
  }

}

