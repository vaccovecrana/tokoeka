package io.vacco.tokoeka.util;

public class TkSquelchParams {

  public long tailTimeMs;
  public double nfSmoothingFactor, nfMultiplier;

  public static TkSquelchParams of(long tailTimeMs, double nfSmoothingFactor, double nfMultiplier) {
    var p = new TkSquelchParams();
    p.tailTimeMs = tailTimeMs;
    p.nfSmoothingFactor = nfSmoothingFactor;
    p.nfMultiplier = nfMultiplier;
    return p;
  }

}
