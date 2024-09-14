package io.vacco.tokoeka.handler;

import io.vacco.tokoeka.schema.dx.TkDxConfig;
import io.vacco.tokoeka.schema.kiwi.TkKiwiConfig;
import io.vacco.tokoeka.spi.*;
import io.vacco.tokoeka.schema.TkConfig;
import org.slf4j.*;

import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import static io.vacco.tokoeka.schema.TkConstants.*;
import static io.vacco.tokoeka.util.TkCommand.*;
import static io.vacco.tokoeka.util.TkFormat.*;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.util.Objects.requireNonNull;

public class TkControlHdl implements TkSocketHdl {

  private static final Logger log = LoggerFactory.getLogger(TkControlHdl.class);

  private final TkConfig config;

  private Consumer<String>  tx;
  private TkAudioHdl        audioHdl;
  private TkWaterfallHdl    waterfallHdl;
  private TkJsonIn          jsonIn;
  private TkConfigPin       configPin;
  protected TkControlPin    controlPin;

  public TkKiwiConfig kiwiConfig;
  public TkDxConfig   dxConfig, dxCommConfig;

  public TkControlHdl(TkConfig config) {
    this.config = requireNonNull(config);
  }

  public void controlEvent(int wsCode, String key, String value, boolean remote, Exception e) {
    if (this.controlPin != null) {
      this.controlPin.onEvent(wsCode, key, value, remote, e);
    }
  }

  private void processKeyValue(String key, String value) {
    switch (key) {
      case last_community_download: log.info(URLDecoder.decode(value, StandardCharsets.UTF_8)); break;
      case bandwidth:       this.config.frequencyMax = parseDouble(value); break;
      case version_maj:     this.config.kiwiMajor = parseInt(value); break;
      case version_min:     this.config.kiwiMinor = parseInt(value); break;
      case freq_offset:     this.config.frequencyOffset = parseDouble(value); break;
      case debian_ver:      this.config.debian_ver = value; break;
      case model:           this.config.model = value; break;
      case platform:        this.config.platform = value; break;
      case ext_clk:         this.config.ext_clk = value; break;
      case abyy:            this.config.abyy = value; break;
      case rx_chans:        this.config.rx_chans = parseInt(value); break;
      case audio_rate:      if (this.audioHdl != null) this.audioHdl.onAudioRate(parseInt(value)); break;
      case sample_rate:     if (this.audioHdl != null) this.audioHdl.onSampleRate(parseDouble(value)); break;
      case load_cfg:        if (this.jsonIn != null) this.kiwiConfig = loadKiwiConfig(value, jsonIn); break;
      case load_dxcfg:      if (this.jsonIn != null) this.dxConfig = loadKiwiDxConfig(value, jsonIn); break;
      case load_dxcomm_cfg: if (this.jsonIn != null) this.dxCommConfig = loadKiwiDxConfig(value, jsonIn); break;
      case cfg_loaded:      if (this.configPin != null) this.configPin.onConfig(kiwiConfig, dxConfig, dxCommConfig); break;
      case badp:
      case too_busy:
      case redirect:
      case down:            this.controlEvent(-1, key, value, true, null); break;
      default:
        if (log.isDebugEnabled()) {
          var val = value == null ? "" : value.trim();
          if (val.length() > 64) {
            val = String.format("%s...", val.substring(0, 64));
          }
          log.debug("Unknown message key/value: {} -> {}", key, val);
        }
    }
  }

  private void processMsg(String body) {
    var params = parseParameters(body);
    if (log.isTraceEnabled()) {
      log.trace(">> {} {} {}", MSG, body, params);
    }
    params.forEach(this::processKeyValue);
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

  @Override public void onOpen(String handShake) {
    if (tx == null) {
      throw  new IllegalStateException("no tx sink, check handler configuration.");
    }
    tx.accept(setAuth(config.username, config.password));
    tx.accept(setIdentity(config.identUser));
  }

  @Override public void onMessage(ByteBuffer data) {
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
        case MSG: processMsg(asString(skip(data, 1))); break;
        case SND: processAudio(data); break;
        case WF:  processWaterfall(skip(data, 1)); break;
        // case "EXT": processExt(asString(skip(data, 1))); break; TODO what should be implemented?
        default: log.warn("Unsupported message tag {} ({})", tag, data.remaining());
      }
    } catch (Exception e) {
      this.onError(e);
    }
  }

  @Override public void onMessage(String message) {}

  @Override public void onClose(int code) {
    this.controlEvent(code, null, null, true, null);
  }

  @Override public void onError(Exception e) {
    this.controlEvent(-1, null, null, false, e);
  }

  public TkControlHdl withAudioHandler(TkAudioHdl hdl) {
    this.audioHdl = requireNonNull(hdl);
    return this;
  }

  public TkControlHdl withWaterfallHandler(TkWaterfallHdl hdl) {
    this.waterfallHdl = requireNonNull(hdl);
    return this;
  }

  public TkControlHdl withJsonIn(TkJsonIn jsonIn) {
    this.jsonIn = requireNonNull(jsonIn);
    return this;
  }

  public TkControlHdl withControlPin(TkControlPin pin) {
    this.controlPin = requireNonNull(pin);
    return this;
  }

  public TkControlHdl withConfigPin(TkConfigPin pin) {
    this.configPin = requireNonNull(pin);
    return this;
  }

  public TkControlHdl withSink(Consumer<String> tx) {
    this.tx = requireNonNull(tx);
    return this;
  }

}
