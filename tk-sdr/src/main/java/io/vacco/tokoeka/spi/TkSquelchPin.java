package io.vacco.tokoeka.spi;

public interface TkSquelchPin {
  void onSquelch(boolean open, byte[] pcm, double signalAvg, double signalThr);
}
