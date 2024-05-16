import io.vacco.shax.logging.ShOption;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiDevice.Info;

public class TkMidi {

  static {
    ShOption.setSysProp(ShOption.IO_VACCO_SHAX_DEVMODE, "true");
    ShOption.setSysProp(ShOption.IO_VACCO_SHAX_LOGLEVEL, "info");
  }

  public static void listMidiDevices() {
    Info[] devicesInfo = MidiSystem.getMidiDeviceInfo();

    if (devicesInfo.length == 0) {
      System.out.println("No MIDI devices found.");
    } else {
      System.out.println("Listing available MIDI devices:");
      for (Info info : devicesInfo) {
        try {

          var device = MidiSystem.getMidiDevice(info);
          System.out.printf("Device: %s, Description: %s, Vendor: %s, Version: %s%n",
              info.getName(), info.getDescription(), info.getVendor(), info.getVersion());
          System.out.printf("    Max Transmitters: %s%n", device.getMaxTransmitters());
          System.out.printf("    Max Receivers: %s%n", device.getMaxReceivers());
        } catch (Exception e) {
          System.out.println("Error accessing device: " + info.getName());
          e.printStackTrace();
        }
      }
    }
  }

  public static void main(String[] args) {
    try {
      // Specify the MIDI device index or modify this to select based on criteria
      MidiDevice.Info[] devicesInfo = MidiSystem.getMidiDeviceInfo();
      if (devicesInfo.length == 0) {
        System.out.println("No MIDI devices found.");
        return;
      }

      var info = devicesInfo[5];

      MidiDevice device = MidiSystem.getMidiDevice(info); // Select the first device
      System.out.println("Opening device: " + info.getName());

      var receiver = new TkMidiInput(info.getName());
      device.getTransmitter().setReceiver(receiver);
      device.open(); // Open the device to start receiving data

      System.out.println("Press ENTER to stop listening...");
      System.in.read(); // Block and wait for user to end listening

      device.close();
      receiver.close();
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Error occurred: " + e.getMessage());
    }
  }

}
