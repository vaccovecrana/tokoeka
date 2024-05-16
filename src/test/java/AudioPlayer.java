import javax.sound.sampled.*;

public class AudioPlayer {

  private SourceDataLine line;

  public AudioPlayer(int sampleRate) {
    try {
      AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
      DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
      line = (SourceDataLine) AudioSystem.getLine(info);
      line.open(format);
      line.start();
    } catch (LineUnavailableException e) {
      throw new IllegalStateException(e);
    }
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
