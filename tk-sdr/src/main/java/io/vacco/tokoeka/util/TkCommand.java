package io.vacco.tokoeka.util;

import io.vacco.tokoeka.schema.TkModulation;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static io.vacco.tokoeka.schema.TkConstants.badp;
import static io.vacco.tokoeka.schema.TkConstants.too_busy;
import static io.vacco.tokoeka.util.TkPair.*;
import static java.lang.String.format;

public class TkCommand {

  public static void setMulti(Consumer<String> tx, String ... commands) {
    if (commands != null) {
      for (var cmd : commands) {
        tx.accept(cmd);
      }
    }
  }

  public static String set(TkPair... pairs) {
    var args = Arrays.stream(pairs).map(TkPair::toKv).collect(Collectors.joining(" "));
    return format("SET %s", args);
  }

  public static String setAudioRate(int arIn, int arOut) {
    return set(ks("AR"), ks("OK"), ki("in", arIn), ki("out", arOut));
  }

  public static String setModulation(TkModulation mod, double frequency) {
    return set(ks("mod", mod.name()), ki("low_cut", mod.lf), ki("high_cut", mod.hf), kd3("freq", frequency));
  }

  public static String setAGC(boolean on, boolean hang, int thresh, int slope, int decay, int gain) {
    return set(
        kb("agc", on), kb("hang", hang),
        ki("thresh", thresh), ki("slope", slope),
        ki("decay", decay), ki("manGain", gain)
    );
  }

  public static String setLittleEndian() {
    return set(ks("little-endian"));
  }

  public static String setGenAttn(int attn) {
    return set(ki("genattn", attn));
  }

  public static String setGenMix(int freq, int mix) {
    return set(ki("gen", freq), ki("mix", mix));
  }

  /**
   * @param thresholdDb from 0dB (disabled) to 40dB
   * @param tailLength 0.0, 0.2, 0.5, 1.0 or 2.0 seconds
   * @return command
   */
  public static String setSquelch(int thresholdDb, double tailLength) {
    return set(ki("squelch", thresholdDb), kd2("param", tailLength));
  }

  public static String setNbGate(int gate) {
    return set(ks("nb", null), ki("type", 0), ki("param", 0), ki("pval", gate));
  }

  public static String setNbThreshold(int threshold) {
    return set(ks("nb", null), ki("type", 0), ki("param", 1), ki("pval", threshold));
  }

  public static String setNrAlgo(int algoId) {
    return set(ks("nr"), ki("algo", algoId));
  }

  /**
   * @param gain from 0.00 to 30.00, approximately, unknown unit.
   * @param alpha from 0.90 to 0.99.
   * @param activeSnr from 2 to 1000. In UI seems like 2dB to 30dB, why?
   * @return command strings
   *
   * TODO I've seen type=0 and type=1 in the actual command, what's that?
   */
  public static String[] setNrAlgoSpecParams(int gain, double alpha, int activeSnr) {
    return new String[] {
        set(ks("nr"), ki("type", 0), ki("param", 0), ki("pval", gain  )),
        set(ks("nr"), ki("type", 0), ki("param", 1), kd3("pval", alpha)),
        set(ks("nr"), ki("type", 0), ki("param", 2), ki("pval", activeSnr))
    };
  }

  public static String setCompression(boolean enable) {
    return set(kb("compression", enable));
  }

  public static String setIQMode() {
    return set(kb("IQ", true));
  }

  public static String setWfSpeed(int speed) {
    return set(ki("wf_speed", speed));
  }

  public static String setWfComp(boolean compression) {
    return set(kb("wf_comp", compression));
  }

  public static String setDeEmp() {
    return set(kb("de_emp", true));
  }

  public static String setInterpolation(int mode) {
    return set(ki("interp", mode));
  }

  public static String setAuth(String username, String password) {
    if (password == null || password.isEmpty()) {
      password = "#";
    }
    return set(ks("auth"), ks("t", username), ks("p", password));
  }

  public static String setIdentity(String identityUser) {
    return set(ks("ident_user", identityUser));
  }

  public static String setKeepAlive() {
    return set(ks("keepalive"));
  }

  public static boolean isKiwiOk(String key, String value) {
    if (key == null) { // not a control message
      return true;
    }
    return Objects.equals(key, badp) && Objects.equals(value, "0");
  }

}
