package io.vacco.tokoeka.spi;

public interface TkAudioPin {
  void onAudio(int flags, int sequenceNumber, int sMeter, double rssi, byte[] rawPcm);
}