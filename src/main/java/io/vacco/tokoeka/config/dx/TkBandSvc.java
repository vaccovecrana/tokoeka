package io.vacco.tokoeka.config.dx;

public class TkBandSvc {

  public String key;
  public String name;
  public String color;
  public String longName;

  @Override public String toString() {
    return String.format("%s %s %s", key, name, color);
  }
}
