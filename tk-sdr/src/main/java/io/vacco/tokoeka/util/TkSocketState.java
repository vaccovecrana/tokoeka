package io.vacco.tokoeka.util;

import java.io.ByteArrayOutputStream;

public class TkSocketState {

  public final ByteArrayOutputStream accumulatedData = new ByteArrayOutputStream();

  /*
   * TODO: these attributes are here so ping/pong based keep-alive
   *   mechanisms can be implemented. However, I'm not going to
   *   provide a default implementation. Maybe in a future release
   *   if there's interest/demand.
   */
  public long     lastPingMs = 0;
  public long     lastPongMs = 0;
  public long     keepAliveMs;
  public int      maxFrameBytes;

  public int      closeCode = Integer.MIN_VALUE;
  public String   closeReason;
  public boolean  closeByRemote;

  public Object   attachment;

  public void markClosed(int closeCode, String closeReason, boolean closeByRemote) {
    this.closeCode = closeCode;
    this.closeReason = closeReason;
    this.closeByRemote = closeByRemote;
  }

  public boolean hasCloseCode() {
    return closeCode != Integer.MIN_VALUE;
  }

  public static TkSocketState of(long keepAliveMs, int maxFrameBytes) {
    var ss = new TkSocketState();
    ss.maxFrameBytes = maxFrameBytes;
    ss.keepAliveMs = keepAliveMs;
    return ss;
  }

  @Override public String toString() {
    return String.format(
      "piMs: %d, poMs: %d, cl: %d, clr: %s, clRm: %s",
      lastPingMs, lastPongMs,
      closeCode == Integer.MIN_VALUE ? -1 : closeCode,
      closeReason, closeByRemote
    );
  }

}
