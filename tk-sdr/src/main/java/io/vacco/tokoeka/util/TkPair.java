package io.vacco.tokoeka.util;

import java.util.Objects;
import static java.lang.String.format;

public class TkPair {

  String key, value;

  public String toKv() {
    if (value == null) {
      return format("%s", key);
    }
    return format("%s=%s", key, value);
  }

  public static TkPair ks(String k, String v) {
    var p = new TkPair();
    p.key = Objects.requireNonNull(k);
    p.value = v;
    return p;
  }

  public static TkPair ks(String k) {
    return ks(k, null);
  }

  public static TkPair ki(String k, int v) {
    return ks(k, format("%d", v));
  }

  public static TkPair kb(String k, boolean v) {
    return ki(k, v ? 1 : 0);
  }

  public static TkPair kd3(String k, double v) {
    return ks(k, format("%.3f", v));
  }

  public static TkPair kd2(String k, double v) {
    return ks(k, format("%.2f", v));
  }

}
