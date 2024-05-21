package io.vacco.tokoeka.util;

import io.vacco.tokoeka.schema.TkSquelchParams;
import io.vacco.tokoeka.spi.TkSquelchPin;
import java.util.Objects;
import static io.vacco.tokoeka.util.TkAudio.signalAverageOf;

public class TkSquelch {

  private final TkSquelchParams params;

  private double threshold;
  private TkSquelchPin pin;
  private long lastSignalTime = 0;
  private boolean squelchOpen = false;
  private double nfAverage = 0; // noise floor average
  private boolean isFirstSample = true;

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
      nfAverage = params.nfSmoothingFactor * signalAvg + (1 - params.nfSmoothingFactor) * nfAverage;
    }
    threshold = nfAverage * params.nfMultiplier;
  }

  public TkSquelch withPin(TkSquelchPin pin) {
    this.pin = Objects.requireNonNull(pin);
    return this;
  }
}
