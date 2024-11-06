package io.vacco.tokoeka.spi;

public interface TkAudioPin {
  void onAudio(
    TkConn conn, int sampleRate, int flags, int sequenceNumber,
    int sMeter, double rssi, byte[] imaPcm, byte[] rawPcm
  );
}