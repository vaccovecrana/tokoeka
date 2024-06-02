package io.vacco.tokoeka.schema;

public class TkSquelchParams {

  public long tailTimeMs;
  public double nfMultiplier;

  public static TkSquelchParams of(long tailTimeMs, double nfMultiplier) {
    var p = new TkSquelchParams();
    p.tailTimeMs = tailTimeMs;
    p.nfMultiplier = nfMultiplier;
    return p;
  }

}
