package io.vacco.tokoeka;

import io.vacco.tokoeka.schema.dx.TkDxConfig;
import io.vacco.tokoeka.schema.kiwi.TkKiwiConfig;
import io.vacco.tokoeka.spi.TkControlPin;
import io.vacco.tokoeka.spi.TkJsonIn;
import io.vacco.tokoeka.schema.TkConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Consumer;

import static io.vacco.tokoeka.util.TkCommand.*;
import static io.vacco.tokoeka.util.TkFormat.*;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

public class TkControlHdl implements Consumer<ByteBuffer> {

  private static final Logger log = LoggerFactory.getLogger(TkControlHdl.class);

  private final TkConfig config;
  private final Consumer<String> tx;

  private TkAudioHdl audioHdl;
  private TkWaterfallHdl waterfallHdl;
  private TkJsonIn jsonIn;
  public  TkControlPin controlPin;

  public TkKiwiConfig kiwiConfig;
  public TkDxConfig dxConfig, dxCommConfig;

  public TkControlHdl(TkConfig config, Consumer<String> tx) {
    this.config = Objects.requireNonNull(config);
    this.tx = Objects.requireNonNull(tx);
  }

  private void processAudio(ByteBuffer data) {
    if (this.audioHdl != null) {
      this.audioHdl.processAudio(data);
    }
  }

  private void processWaterfall(ByteBuffer data) {
    if (this.waterfallHdl != null) {
      this.waterfallHdl.processWaterfall(data);
    }
  }

  private void processKeyValue(String key, String value) {
    switch (key) {
      case "last_community_download": log.info(URLDecoder.decode(value, StandardCharsets.UTF_8)); break;
      case "bandwidth":       this.config.frequencyMax = parseDouble(value); break;
      case "version_maj":     this.config.kiwiMajor = parseInt(value); break;
      case "version_min":     this.config.kiwiMinor = parseInt(value); break;
      case "freq_offset":     this.config.frequencyOffset = parseDouble(value); break;
      case "debian_ver":      this.config.debian_ver = value; break;
      case "model":           this.config.model = value; break;
      case "platform":        this.config.platform = value; break;
      case "ext_clk":         this.config.ext_clk = value; break;
      case "abyy":            this.config.abyy = value; break;
      case "rx_chans":        this.config.rx_chans = parseInt(value); break;
      case "audio_rate":      if (this.audioHdl != null) this.audioHdl.onAudioRate(parseInt(value)); break;
      case "sample_rate":     if (this.audioHdl != null) this.audioHdl.onSampleRate(parseDouble(value)); break;
      case "load_cfg":        if (this.jsonIn != null) this.kiwiConfig = loadKiwiConfig(value, jsonIn); break;
      case "load_dxcfg":      if (this.jsonIn != null) this.dxConfig = loadKiwiDxConfig(value, jsonIn); break;
      case "load_dxcomm_cfg": if (this.jsonIn != null) this.dxCommConfig = loadKiwiDxConfig(value, jsonIn); break;
      case "cfg_loaded":      break; // cool...
      case "badp":
        if (!"0".equals(value) && this.controlPin != null) {
          controlPin.onEvent(-1, key, value, true, null);
        }
        break;
      case "too_busy":
      case "redirect":
      case "down":
        if (this.controlPin != null) {
          controlPin.onEvent(-1, key, value, true, null);
        }
        break;
      default:
        if (log.isDebugEnabled()) {
          log.debug("Unknown message key/value: {} -> {}", key, value == null ? "" : value.trim());
        }
    }
  }

  private void processMsg(String body) {
    var params = parseParameters(body);
    if (log.isDebugEnabled()) {
      log.debug(">> MSG {} {}", body, params);
    }
    params.forEach(this::processKeyValue);
  }

  @Override public void accept(ByteBuffer data) {
    if (data == null || data.remaining() < 3) {
      log.error("No data, or received data is too short to contain a valid tag");
      return;
    }
    try {
      var tagBytes = new byte[3];
      data.get(tagBytes);
      var tag = new String(tagBytes, StandardCharsets.UTF_8);
      data = data.slice();

      if (log.isTraceEnabled()) {
        log.trace("{} ({})", tag, data.remaining());
      }

      switch (tag) {
        case "MSG": processMsg(asString(skip(data, 1))); break;
        case "SND": processAudio(data); break;
        case "W/F": processWaterfall(skip(data, 1)); break;
        // case "EXT": processExt(asString(skip(data, 1))); break; TODO what should be implemented?
        default: log.warn("Unsupported message tag {} ({})", tag, data.remaining());
      }
    } catch (Exception e) {
      if (this.controlPin != null) {
        controlPin.onEvent(-1, null, null, false, e);
      }
    }
  }

  public void onAuth() {
    tx.accept(setAuth(config.username, config.password));
    tx.accept(setIdentity(config.identUser));
  }

  public TkControlHdl withAudioHandler(TkAudioHdl hdl) {
    this.audioHdl = Objects.requireNonNull(hdl);
    return this;
  }

  public TkControlHdl withWaterfallHandler(TkWaterfallHdl hdl) {
    this.waterfallHdl = Objects.requireNonNull(hdl);
    return this;
  }

  public TkControlHdl withJsonIn(TkJsonIn jsonIn) {
    this.jsonIn = Objects.requireNonNull(jsonIn);
    return this;
  }

  public TkControlHdl withControlPin(TkControlPin pin) {
    this.controlPin = Objects.requireNonNull(pin);
    return this;
  }

}
