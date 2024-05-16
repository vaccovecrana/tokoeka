package io.vacco.tokoeka.config.dx;

public class TkBand {

  public String name;
  public String svc;
  public String sel;

  public double min;
  public double max;
  public int itu;
  public int chan;

  @Override public String toString() {
    return String.format("%s %s %s %.4f %.4f", svc, name, sel, min, max);
  }
}
