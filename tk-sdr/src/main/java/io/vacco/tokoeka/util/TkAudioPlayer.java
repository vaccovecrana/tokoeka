package io.vacco.tokoeka.util;

import javax.sound.sampled.*;

public class TkAudioPlayer {

  private SourceDataLine line;

  public TkAudioPlayer(int sampleRate, int bitRate, int channels, boolean signed, boolean bigEndian) {
    try {
      AudioFormat format = new AudioFormat(sampleRate, bitRate, channels, signed, bigEndian);
      DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
      line = (SourceDataLine) AudioSystem.getLine(info);
      line.open(format);
      line.start();
    } catch (LineUnavailableException e) {
      throw new IllegalStateException(e);
    }
  }

  public TkAudioPlayer(int sampleRate, int bitRate, int channels) {
    this(sampleRate, bitRate, channels, true, false);
  }

  public void play(byte[] audioData) {
    if (line != null) {
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
