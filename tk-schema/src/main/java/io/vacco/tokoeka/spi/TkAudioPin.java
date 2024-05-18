package io.vacco.tokoeka.spi;

public interface TkAudioPin {
  void onAudio(int sampleRate, int flags, int sequenceNumber, int sMeter, double rssi, byte[] rawPcm);
}