package io.vacco.tokoeka.handler;

import io.vacco.tokoeka.spi.TkWfPin;
import io.vacco.tokoeka.util.TkTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;
import java.util.function.Consumer;

import static io.vacco.tokoeka.util.TkCommand.setKeepAlive;

public class TkWaterfallHdl {

  private static final Logger log = LoggerFactory.getLogger(TkWaterfallHdl.class);

  private final TkWfPin wfPin;
  private final TkTimer timer;
  private final Consumer<String> tx;

  public TkWaterfallHdl(Consumer<String> tx, TkWfPin wfPin) {
    this.tx = Objects.requireNonNull(tx);
    this.wfPin = Objects.requireNonNull(wfPin);
    this.timer = new TkTimer(3000, () -> tx.accept(setKeepAlive()));
  }

  public void processWaterfall(ByteBuffer data) {
    if (data.remaining() < 12) {
      log.warn("Received waterfall data is too short");
      return;
    }
    data.order(ByteOrder.LITTLE_ENDIAN);

    var xBin = data.getInt(); // x-bin start index for the waterfall data
    var flags = data.getInt(); // flags associated with the waterfall data
    var sequenceNumber = data.getInt(); // sequence number of the waterfall data packet

    if (data.hasRemaining()) { // Remaining buffer contains waterfall data
      var wfData = new byte[data.remaining()];
      data.get(wfData);
      wfPin.onWaterfallData(xBin, sequenceNumber, flags, wfData);
    }

    timer.update();
  }

}
