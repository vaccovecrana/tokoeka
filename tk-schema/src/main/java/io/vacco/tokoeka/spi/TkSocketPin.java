package io.vacco.tokoeka.spi;

public interface TkSocketPin {
  void onClose(int code, String reason, boolean remote, Exception e);
}
