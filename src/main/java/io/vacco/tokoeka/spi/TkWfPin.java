package io.vacco.tokoeka.spi;

public interface TkWfPin {
  void onWaterfallData(int xBin, int sequenceNumber, int flags, byte[] rawWfData);
}
