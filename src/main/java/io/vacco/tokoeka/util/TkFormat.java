package io.vacco.tokoeka.util;

import io.vacco.tokoeka.config.dx.TkDxConfig;
import io.vacco.tokoeka.config.kiwi.TkKiwiConfig;
import io.vacco.tokoeka.spi.TkJsonIn;

import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class TkFormat {

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

}
