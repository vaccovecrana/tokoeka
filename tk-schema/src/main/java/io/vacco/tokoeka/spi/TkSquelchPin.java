package io.vacco.tokoeka.spi;

public interface TkSquelchPin {
  void onUpdate(boolean open, byte[] pcm, double signalAvg, double signalThr);
}
