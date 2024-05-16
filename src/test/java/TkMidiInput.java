import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.midi.*;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class TkMidiInput implements Receiver {

  private static final Logger log = LoggerFactory.getLogger(TkMidiInput.class);

  private String deviceName;

  public TkMidiInput(String deviceName) {
    this.deviceName = deviceName;
  }

  public static String md5OfIntegers(int a, int b, int c, int d) {
    try {
      // Create a ByteBuffer of size of 4 integers (4 bytes each)
      var buffer = ByteBuffer.allocate(4 * 4);
      buffer.putInt(a);
      buffer.putInt(b);
      buffer.putInt(c);
      buffer.putInt(d);

      // Get the byte array from the ByteBuffer
      byte[] bytes = buffer.array();

      // Get MessageDigest instance for MD5
      var md = MessageDigest.getInstance("MD5");
      byte[] digest = md.digest(bytes);

      // Convert the byte array to a hex string
      return bytesToHex(digest);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("MD5 algorithm not available.", e);
    }
  }

  private static String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }


  @Override
  public void send(MidiMessage message, long timeStamp) {
    if (message instanceof ShortMessage) {
      var sm = (ShortMessage) message;
      var id = md5OfIntegers(sm.getCommand(), sm.getChannel(), sm.getData1(), sm.getData2());
      log.info(
          "{} - Device: {}, Command: {}, Channel: {}, Data1: {}, Data2: {}, Timestamp: {}",
          id, deviceName, sm.getCommand(), sm.getChannel(), sm.getData1(), sm.getData2(), timeStamp
      );
    }
  }

  @Override
  public void close() {
    System.out.println("Closed MIDI input for device: " + deviceName);
  }
}
