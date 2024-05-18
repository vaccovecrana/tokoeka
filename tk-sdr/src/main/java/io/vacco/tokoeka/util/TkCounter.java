package io.vacco.tokoeka.util;

import java.util.Objects;

public class TkCounter {

  private final int max;
  private final Runnable r;
  private int k = 0;

  public TkCounter(int max, Runnable r) {
    this.max = max;
    this.r = Objects.requireNonNull(r);
  }

  public void update() {
    k++;
    if (k == max) {
      r.run();
      k = 0;
    }
  }
}
