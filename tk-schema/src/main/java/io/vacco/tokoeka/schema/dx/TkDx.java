package io.vacco.tokoeka.schema.dx;

public class TkDx {

  public int key;
  public String name;
  public String color;

  @Override public String toString() {
    return String.format("%d %s %s", key, name, color);
  }
}
