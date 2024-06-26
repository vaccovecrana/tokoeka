package io.vacco.tokoeka.schema.kiwi;
import java.util.Map;

public class TkKiwiConfig {

  public long inactivity_timeout_mins;
  public long rx_asl;
  public long max_freq;
  public long freq_offset;
  public long ip_limit_mins;
  public long S_meter_cal;
  public long waterfall_cal;
  public long chan_no_pwd;
  public long clk_adj;
  public long sdr_hu_lo_kHz;
  public long sdr_hu_hi_kHz;
  public long tdoa_nchans;
  public long ext_api_nchans;
  public long ext_ADC_freq;
  public long S_meter_OV_counts;
  public long overload_mute;
  public long n_camp;
  public long snr_meas_interval_hrs;
  public long ident_len;
  public long dx_default_db;
  public long nb_algo;
  public long nb_wf;
  public long nb_gate;
  public long nb_thresh;
  public long nb_taps;
  public long nb_samps;
  public long nr_algo;
  public long nr_de;
  public long nr_an;
  public long nr_wdspDeTaps;
  public long nr_wdspDeDelay;
  public long nr_wdspDeGain;
  public long nr_wdspDeLeak;
  public long nr_wdspAnTaps;
  public long nr_wdspAnDelay;
  public long nr_wdspAnGain;
  public long nr_wdspAnLeak;
  public long nr_origDeDelay;
  public long nr_origAnDelay;
  public long nr_specGain;
  public long nr_specSNR;
  public long ethernet_speed;
  public long ethernet_mtu;
  public long sdr_hu_dom_sel;
  public long SPI_clock;
  public long led_brightness;
  public long CAT_baud;
  public long rf_attn_allow;

  public double DC_offset_I;
  public double DC_offset_Q;
  public double DC_offset_20kHz_I;
  public double DC_offset_20kHz_Q;
  public double ADC_clk2_corr;
  public double nb_thresh2;
  public double nr_origDeBeta;
  public double nr_origDeDecay;
  public double nr_origAnBeta;
  public double nr_origAnDecay;
  public double nr_specAlpha;

  public boolean contact_admin;
  public boolean ext_ADC_clk;
  public boolean no_wf;
  public boolean test_webserver_prio;
  public boolean test_deadline_update;
  public boolean disable_recent_changes;
  public boolean webserver_caching;
  public boolean agc_thresh_smeter;
  public boolean snr_local_time;
  public boolean any_preempt_autorun;
  public boolean show_geo;
  public boolean show_1Hz;
  public boolean spectral_inversion;
  public boolean show_geo_city;
  public boolean require_id;
  public boolean require_id_setup;

  public String status_msg;
  public String rx_name;
  public String rx_device;
  public String rx_location;
  public String rx_grid;
  public String rx_antenna;
  public String rx_gps;
  public String server_url;
  public String admin_email;
  public String owner_info;
  public String reason_disabled;
  public String tdoa_id;
  public String reason_kicked;
  public String sdr_hu_dom_name;
  public String sdr_hu_dom_ip;
  public String panel_readme;

  public Map<String, String> index_html_params;
  public Map<String, TkPassBand> passbands;

  public TkInit init;
  public TkDrm drm;
  public TkWspr wspr;
  public TkFt8 ft8;

  public TkBool tdoa;
  public TkBool ale_2g;
  public TkBool cw;
  public TkBool digi_modes;
  public TkBool fax;
  public TkBool fft;
  public TkBool fsk;
  public TkBool hfdl;
  public TkBool iframe;
  public TkBool iq_display;
  public TkBool loran_c;
  public TkBool navtex;
  public TkBool prefs;
  public TkBool sig_gen;
  public TkBool S_meter;
  public TkBool sstv;
  public TkBool timecode;
  public TkBool ant_switch;

}
