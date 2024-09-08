package io.vacco.tokoeka.audio;

import javax.sound.sampled.*;

public class TkAudioPlayer {

  private SourceDataLine line;
  private int bitRate, channels;
  private boolean signed, bigEndian;
  private int configuredSampleRate;

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
        this.configuredSampleRate = sampleRate;
        var format = new AudioFormat(sampleRate, bitRate, channels, signed, bigEndian);
        var info = new DataLine.Info(SourceDataLine.class, format);
        line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();
      } catch (LineUnavailableException e) {
        throw new IllegalStateException(e);
      }
    }

    if (sampleRate != this.configuredSampleRate) {
      audioData = resampleAudio(audioData, sampleRate, this.configuredSampleRate);
    }

    line.write(audioData, 0, audioData.length);
  }

  private byte[] resampleAudio(byte[] input, int inputSampleRate, int outputSampleRate) {
    int inputLength = input.length;
    int frameSize = bitRate / 8 * channels;
    int inputFrames = inputLength / frameSize;
    int outputFrames = (int) (((double) inputFrames / inputSampleRate) * outputSampleRate);
    int outputLength = outputFrames * frameSize;
    var output = new byte[outputLength];

    for (int i = 0; i < outputFrames; i++) {
      int inputIndex = (int) (((double) i / outputSampleRate) * inputSampleRate) * frameSize;
      int outputIndex = i * frameSize;
      System.arraycopy(input, inputIndex, output, outputIndex, frameSize);
    }

    return output;
  }

  public void close() {
    if (line != null) {
      line.drain();
      line.close();
    }
  }
}
