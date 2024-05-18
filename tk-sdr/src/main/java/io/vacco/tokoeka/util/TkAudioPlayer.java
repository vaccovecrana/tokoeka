package io.vacco.tokoeka.util;

import javax.sound.sampled.*;

public class TkAudioPlayer {

  private SourceDataLine line;
  private int bitRate, channels;
  private boolean signed, bigEndian;

  public TkAudioPlayer(int bitRate, int channels, boolean signed, boolean bigEndian) {
    this.bitRate = bitRate;
    this.channels = channels;
    this.signed = signed;
    this.bigEndian = bigEndian;
  }

  public TkAudioPlayer(int bitRate, int channels) {
    this(bitRate, channels, true, false);
  }

  /**
   * Allows for deferred audio playback. That is, initialize the audio line only when
   * we are sure about what sample rate to use.
   * @param sampleRate sample rate
   * @param audioData raw pcm
   */
  public void play(int sampleRate, byte[] audioData) {
    if (line == null) {
      try {
        var format = new AudioFormat(sampleRate, bitRate, channels, signed, bigEndian);
        var info = new DataLine.Info(SourceDataLine.class, format);
        line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();
      } catch (LineUnavailableException e) {
        throw new IllegalStateException(e);
      }
    } else {
      line.write(audioData, 0, audioData.length);
    }
  }

  public void close() {
    if (line != null) {
      line.drain();
      line.close();
    }
  }
}
