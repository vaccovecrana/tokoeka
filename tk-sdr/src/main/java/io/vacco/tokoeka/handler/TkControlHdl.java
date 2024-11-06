package io.vacco.tokoeka.handler;

import io.vacco.tokoeka.schema.dx.TkDxConfig;
import io.vacco.tokoeka.schema.kiwi.TkKiwiConfig;
import io.vacco.tokoeka.spi.*;
import io.vacco.tokoeka.schema.TkConfig;
import io.vacco.tokoeka.util.*;
import org.slf4j.*;

import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import static io.vacco.tokoeka.schema.TkConstants.*;
import static io.vacco.tokoeka.util.TkCommand.*;
import static io.vacco.tokoeka.util.TkFormat.*;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.util.Objects.requireNonNull;

public class TkControlHdl implements TkSocketHdl {

  private static final Logger log = LoggerFactory.getLogger(TkControlHdl.class);

  private final TkConfig config;

  private   TkAudioHdl      audioHdl;
  private   TkWaterfallHdl  waterfallHdl;
  private   TkJsonIn        jsonIn;
  private   TkConfigPin     configPin;
  protected TkSdrPin        sdrPin;

  private Function<TkConn, Boolean> controlFn;

  public TkKiwiConfig kiwiConfig;
  public TkDxConfig   dxConfig, dxCommConfig;

  public TkControlHdl(TkConfig config) {
    this.config = requireNonNull(config);
  }

  public void sdrEvent(TkConn conn, String key, String value, Exception e, boolean ping, boolean pong) {
    if (this.sdrPin != null) {
      this.sdrPin.onEvent(conn, key, value, e, ping, pong);
    }
  }

  private void processKeyValue(TkConn conn, String key, String value) {
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
      case down:            this.sdrEvent(conn, key, value, null, false, false); break;
      default:
        if (log.isDebugEnabled()) {
          log.debug("Unknown message key/value: {} -> {}", key, shorten(value));
        }
    }
  }

  private void processMsg(TkConn conn, String body) {
    var params = parseParameters(body);
    if (log.isTraceEnabled()) {
      log.trace(">> {} {} {}", MSG, shorten(body), shorten(params.toString()));
    }
    params.forEach((key, value) -> processKeyValue(conn, key, value));
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

  @Override public void onOpen(TkConn conn, String handShake) {
    conn.accept(setAuth(config.username, config.password));
    conn.accept(setIdentity(config.identUser));
  }

  @Override public void onMessage(TkConn conn, ByteBuffer data) {
    if (this.controlFn != null && !this.controlFn.apply(conn)) {
      conn.close(TkSockets.WsCloseGoAway);
      return;
    }
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
        case MSG: processMsg(conn, asString(skip(data, 1))); break;
        case SND: processAudio(data); break;
        case WF:  processWaterfall(skip(data, 1)); break;
        // case "EXT": processExt(asString(skip(data, 1))); break; TODO what should be implemented?
        default: log.warn("Unsupported message tag {} ({})", tag, data.remaining());
      }
    } catch (Exception e) {
      this.onError(conn, e);
    }
  }

  @Override public void onMessage(TkConn conn, String message) {}

  @Override public void onClose(TkConn conn) {
    this.sdrEvent(conn, null, null, null, false, false);
  }

  @Override public void onError(TkConn conn, Exception e) {
    this.sdrEvent(conn, null, null, e, false, false);
  }

  @Override public void onPing(TkConn conn) {
    this.sdrEvent(conn, null, null, null, true, false);
  }

  @Override public void onPong(TkConn conn) {
    this.sdrEvent(conn, null, null, null, false, true);
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

  public TkControlHdl withSdrPin(TkSdrPin pin) {
    this.sdrPin = requireNonNull(pin);
    return this;
  }

  public TkControlHdl withConfigPin(TkConfigPin pin) {
    this.configPin = requireNonNull(pin);
    return this;
  }

  public TkControlHdl withControlFn(Function<TkConn, Boolean> controlFn) {
    this.controlFn = requireNonNull(controlFn);
    return this;
  }

}
