package io.vacco.tokoeka.audio;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TkNormalize {

  private final List<byte[]> audioChunks = new ArrayList<>();

  public void update(byte[] pcm) {
    audioChunks.add(pcm);
  }

  public Iterator<byte[]> close() {
    var normalizedChunks = new ArrayList<byte[]>();
    int runningPeak = extractRunningPeak();
    var normalizationFactor = calculateNormalizationFactor(runningPeak);

    for (byte[] chunk : audioChunks) {
      var samples = byteArrayToShortArray(chunk);
      var normalizedSamples = new short[samples.length];
      for (int i = 0; i < samples.length; i++) {
        int normalizedSample = (int) (samples[i] * normalizationFactor);
        normalizedSamples[i] = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, normalizedSample));
      }
      normalizedChunks.add(shortArrayToByteArray(normalizedSamples));
    }

    return normalizedChunks.iterator();
  }

  private int extractRunningPeak() {
    int runningPeak = 0;
    for (var chunk : audioChunks) {
      var samples = byteArrayToShortArray(chunk);
      for (var sample : samples) {
        int absValue = Math.abs(sample);
        if (absValue > runningPeak) {
          runningPeak = absValue;
        }
      }
    }
    return runningPeak;
  }

  private short[] byteArrayToShortArray(byte[] byteArray) {
    var shortArray = new short[byteArray.length / 2];
    ByteBuffer.wrap(byteArray).order(java.nio.ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortArray);
    return shortArray;
  }

  private byte[] shortArrayToByteArray(short[] shortArray) {
    var byteArray = new byte[shortArray.length * 2];
    ByteBuffer.wrap(byteArray).order(java.nio.ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(shortArray);
    return byteArray;
  }

  private float calculateNormalizationFactor(int currentPeak) {
    return 32767.0f / currentPeak;
  }

}
