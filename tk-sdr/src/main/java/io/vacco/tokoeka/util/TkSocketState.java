package io.vacco.tokoeka.util;

import java.io.ByteArrayOutputStream;

public class TkSocketState {

  public final ByteArrayOutputStream accumulatedData = new ByteArrayOutputStream();

  public long   lastPingMs = 0;
  public long   lastPongMs = 0;
  public long   keepAliveMs;
  public int    maxFrameBytes;
  public Object attachment;

  public static TkSocketState of(long keepAliveMs, int maxFrameBytes) {
    var ss = new TkSocketState();
    ss.maxFrameBytes = maxFrameBytes;
    ss.keepAliveMs = keepAliveMs;
    return ss;
  }

  @Override public String toString() {
    return String.format("lpi: %d, lpo: %d", lastPingMs, lastPongMs);
  }

}
