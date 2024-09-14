package io.vacco.tokoeka;

import io.vacco.shax.logging.ShOption;
import j8spec.UnsafeBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.*;

public class TkLogging {

  private static Logger log;

  public static void initLog() {
    ShOption.setSysProp(ShOption.IO_VACCO_SHAX_DEVMODE, "true");
    ShOption.setSysProp(ShOption.IO_VACCO_SHAX_LOGLEVEL, "trace");
    log = LoggerFactory.getLogger(TkLogging.class);
  }

  public static UnsafeBlock localTest(UnsafeBlock b) {
    if (!GraphicsEnvironment.isHeadless()) {
      return b;
    }
    return () -> log.info("CI/CD build. Nothing to do.");
  }

}
