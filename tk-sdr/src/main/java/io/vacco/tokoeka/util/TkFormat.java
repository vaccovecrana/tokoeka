package io.vacco.tokoeka.util;

import io.vacco.tokoeka.schema.dx.TkDxConfig;
import io.vacco.tokoeka.schema.kiwi.TkKiwiConfig;
import io.vacco.tokoeka.spi.TkJsonIn;

import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class TkFormat {

  public static int DefaultMessageShorten = 64;

  public static String asString(ByteBuffer bytes) {
    return StandardCharsets.UTF_8.decode(bytes).toString();
  }

  public static ByteBuffer skip(ByteBuffer b, int n) {
    for (int i = 0; i < n; i++) {
      b.get();
    }
    return b;
  }

  public static Map<String, String> parseParameters(String body) {
    var params = new LinkedHashMap<String, String>();
    var pairs = body.split(" ");
    for (var pair : pairs) {
      var kv = pair.split("=");
      if (kv.length == 2) {
        params.put(kv[0], kv[1]);
      } else if (kv.length == 1) {
        params.put(kv[0], null);
      }
    }
    return params;
  }

  public static <T> T load(String data, Class<T> type, TkJsonIn jsonIn) {
    var json = URLDecoder.decode(data, StandardCharsets.UTF_8);
    return jsonIn.fromJson(json, type);
  }

  public static TkKiwiConfig loadKiwiConfig(String data, TkJsonIn jsonIn) {
    return load(data, TkKiwiConfig.class, jsonIn);
  }

  public static TkDxConfig loadKiwiDxConfig(String data, TkJsonIn jsonIn) {
    return load(data, TkDxConfig.class, jsonIn);
  }

  public static String shorten(String msg, int maxLength) {
    if (msg == null) {
      return "null";
    }
    msg = msg.trim();
    if (msg.length() <= maxLength || maxLength == 1) {
      return msg;
    }
    var ml2 = maxLength / 2;
    var v0 = msg.substring(0, ml2);
    var v1 = msg.substring(msg.length() - ml2);
    return String.format("%s...%s", v0, v1);
  }

  public static String shorten(String msg) {
    return shorten(msg, DefaultMessageShorten);
  }

}
