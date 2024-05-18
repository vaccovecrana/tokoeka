package io.vacco.tokoeka.spi;

public interface TkControlPin {
  void onEvent(int code, String key, String value, boolean remote, Exception e);
}
