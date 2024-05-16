package io.vacco.tokoeka.config;

public class TkConfig {

  public int kiwiMajor, kiwiMinor, rx_chans;
  public String debian_ver, model, platform, ext_clk, abyy;

  public String identUser, username, password;

  public TkModulation modulation;

  public int sampleRateOut = 44100;
  public double sampleRate;

  public double frequency, frequencyOffset = 0.0, frequencyMax = 30e3;

  public boolean agcOn, agcHang;
  public int     agcThresh, agcSlope, agcDecay, agcGain;

  public int     squelchThresholdDb = 0;
  public double  squelchTailLength = 0.00;

  public boolean nb, nbTest;
  public int     nbGate, nbThreshold;

  public int    nrAlgoId = 0;

  public int    nrSpecGain; // algoId is 3 for 'spec' NR.
  public double nrSpecAlpha;
  public int    nrSpecActiveSnr;

  public boolean compression;
  public boolean iqMode;

  public int wfSpeed;
  public boolean wfCompression;

  public boolean deEmp;
  public int interpolation;

}
