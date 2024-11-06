package io.vacco.tokoeka.util;

import java.util.Objects;

public class TkTimer {

  private final long intervalMs;
  private final Runnable r;
  private long lastRunMs;

  public TkTimer(long intervalMs, Runnable r) {
    this.intervalMs = intervalMs;
    this.r = Objects.requireNonNull(r);
    this.lastRunMs = System.currentTimeMillis();
  }

  public void update() {
    long currentTime = System.currentTimeMillis();
    if ((currentTime - lastRunMs) >= intervalMs) {
      r.run();
      lastRunMs = currentTime;
    }
  }

  public static boolean nowMsDiffLt(long t0, long diffMs) {
    var tN = System.currentTimeMillis();
    return (tN - t0) < diffMs;
  }

}
