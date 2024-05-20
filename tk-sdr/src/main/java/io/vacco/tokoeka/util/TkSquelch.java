package io.vacco.tokoeka.util;

import io.vacco.tokoeka.spi.TkSquelchPin;
import java.util.Objects;
import static io.vacco.tokoeka.util.TkAudio.signalAverageOf;

public class TkSquelch {

  private double threshold;
  private TkSquelchPin pin;
  private final long tailTimeMs;
  private final double smoothingFactor;

  private long lastSignalTime = 0;
  private boolean squelchOpen = false;

  private final double nfMultiplier;
  private double nfAverage = 0; // noise floor average
  private boolean isFirstSample = true;

  public TkSquelch(long tailTimeMs, double smoothingFactor, double nfMultiplier) {
    this.tailTimeMs = tailTimeMs;
    this.smoothingFactor = smoothingFactor;
    this.nfMultiplier = nfMultiplier;
  }

  public void processAudio(byte[] pcm) {
    var currentTime = System.currentTimeMillis();
    var signalAvg = signalAverageOf(pcm);

    if (signalAvg > threshold) {
      lastSignalTime = currentTime;
      squelchOpen = true;
    } else if ((currentTime - lastSignalTime) > tailTimeMs) {
      if (squelchOpen && pin != null) {
        pin.onUpdate(false, pcm, signalAvg, threshold);
      }
      squelchOpen = false;
    }
    if (squelchOpen && pin != null) {
      pin.onUpdate(true, pcm, signalAvg, threshold);
    }

    updateNoiseFloor(signalAvg);
  }

  private void updateNoiseFloor(double signalAvg) {
    if (isFirstSample) {
      nfAverage = signalAvg;
      isFirstSample = false;
    } else {
      nfAverage = smoothingFactor * signalAvg + (1 - smoothingFactor) * nfAverage;
    }
    threshold = nfAverage * nfMultiplier;
  }

  public TkSquelch withPin(TkSquelchPin pin) {
    this.pin = Objects.requireNonNull(pin);
    return this;
  }
}
