package io.vacco.tokoeka.spi;

import java.io.*;
import java.lang.reflect.Type;

public interface TkJsonIn {

  <T> T fromJson(Reader r, Type knownType);

  default <T> T fromJson(String s, Type knownType) {
    return fromJson(new StringReader(s), knownType);
  }

}
