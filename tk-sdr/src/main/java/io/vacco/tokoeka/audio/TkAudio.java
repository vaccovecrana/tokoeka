package io.vacco.tokoeka.audio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class TkAudio {

  public static double removeFreqOffset(double freq, double freqOffset, double maxFreq) {
    var fMin = freqOffset;
    var fMax = freqOffset + maxFreq;
    if (freq < fMin || freq > fMax) {
      throw new IllegalStateException(String.format(
          "Frequency must be between %.3fkHz and %.3fkHz", fMin, fMax
      ));
    }
    return freq - freqOffset;
  }

  public static double signalAverageOf(byte[] audioData) {
    var buffer = ByteBuffer.wrap(audioData);
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    var sum = 0;
    var count = 0;
    while (buffer.hasRemaining()) {
      var sample = buffer.getShort();
      sum += Math.abs(sample);
      count++;
    }
    return sum / (double) count;
  }

}
