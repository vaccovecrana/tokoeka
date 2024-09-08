package io.vacco.tokoeka.spi;

import io.vacco.tokoeka.schema.dx.TkDxConfig;
import io.vacco.tokoeka.schema.kiwi.TkKiwiConfig;

public interface TkConfigPin {
  void onConfig(TkKiwiConfig kiwiConfig, TkDxConfig dxConfig, TkDxConfig dxCommConfig);
}
