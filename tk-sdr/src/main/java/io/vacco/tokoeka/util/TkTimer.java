package io.vacco.tokoeka.util;

import java.util.Objects;
import java.util.function.Consumer;

public class TkTimer<T> {

  private final long intervalMs;
  private final Consumer<T> runCons;
  private long lastRunMs = -1;

  public TkTimer(long intervalMs, Consumer<T> runCons) {
    this.intervalMs = intervalMs;
    this.runCons = Objects.requireNonNull(runCons);
  }

  public void update(T arg) {
    long currentTime = System.currentTimeMillis();
    var isFirstRun = lastRunMs == -1;
    var isNextRun = (currentTime - lastRunMs) >= intervalMs;
    if (isFirstRun || isNextRun) {
      runCons.accept(arg);
      lastRunMs = currentTime;
    }
  }

  public static boolean nowMsDiffLt(long t0, long diffMs) {
    var tN = System.currentTimeMillis();
    return (tN - t0) < diffMs;
  }

}
