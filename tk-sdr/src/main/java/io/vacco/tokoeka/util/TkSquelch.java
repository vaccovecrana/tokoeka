package io.vacco.tokoeka.util;

import io.vacco.tokoeka.spi.TkSquelchPin;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

import static io.vacco.tokoeka.util.TkAudio.signalAverageOf;

public class TkSquelch {

  public double threshold;

  private final TkSquelchPin pin;
  private final long fallOffTimeMs;

  private long    lastSignalTime = 0;
  private boolean squelchOpen = false;

  private boolean sampleNoiseFloor = false;
  private long    noiseFloorSampleEndTime = 0;
  private long    noiseFloorSum = 0;
  private int     noiseFloorCount = 0;
  private double  noiseFloorMultiplier = 1.00;

  public TkSquelch(double threshold, TkSquelchPin pin, double fallOffSeconds) {
    this.threshold = threshold;
    this.pin = Objects.requireNonNull(pin);
    this.fallOffTimeMs = (long) (fallOffSeconds * 1000);
  }

  public void processAudio(byte[] pcm) {
    var currentTime = System.currentTimeMillis();
    if (sampleNoiseFloor) {
      adjustNoiseFloor(pcm);
      if (currentTime >= noiseFloorSampleEndTime) {
        finalizeNoiseFloor();
        sampleNoiseFloor = false;
      }
      return;
    }

    var signalAvg = signalAverageOf(pcm);
    if (signalAvg > threshold) {
      lastSignalTime = currentTime;
      squelchOpen = true;
    } else if ((currentTime - lastSignalTime) > fallOffTimeMs) {
      if (squelchOpen) {
        pin.onUpdate(false, pcm, signalAvg);
      }
      squelchOpen = false;
    }
    if (squelchOpen) {
      pin.onUpdate(true, pcm, signalAvg);
    }
  }

  private void adjustNoiseFloor(byte[] audioData) {
    var buffer = ByteBuffer.wrap(audioData);
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    while (buffer.hasRemaining()) {
      short sample = buffer.getShort();
      noiseFloorSum += Math.abs(sample);
      noiseFloorCount++;
    }
  }

  private void finalizeNoiseFloor() {
    if (noiseFloorCount > 0) {
      double averageNoiseLevel = noiseFloorSum / (double) noiseFloorCount;
      threshold = averageNoiseLevel * noiseFloorMultiplier;
    }
    noiseFloorSum = 0;
    noiseFloorCount = 0;
  }

  public void detectNoiseFloor(int sampleLengthMs, double noiseFloorMultiplier) {
    this.sampleNoiseFloor = true;
    this.noiseFloorSampleEndTime = System.currentTimeMillis() + sampleLengthMs;
    this.noiseFloorSum = 0;
    this.noiseFloorCount = 0;
    this.noiseFloorMultiplier = noiseFloorMultiplier;
  }
}
