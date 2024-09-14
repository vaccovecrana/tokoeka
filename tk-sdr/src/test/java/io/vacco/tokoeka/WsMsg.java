package io.vacco.tokoeka;

public class WsMsg {

  public String type;
  public double time;
  public int opcode;
  public String data; // Base64

  @Override public String toString() {
    return String.format("%.4f %d %s (%d)", time, opcode, type, data.length());
  }

}
