package io.vacco.tokoeka.config;

public enum TkModulation {
  am(-4900, 4900), amn(-2500, 2500), amw(-6000, 6000),
  sam(-4900, 4900), sal(-4900, 0), sau(0, 4900), sas(-4900, 4900),
  qam(-4900, 4900),
  drm(-5000, 5000),
  lsb(-2700, -300), lsn(-2400, -300),
  usb(300, 2700), usn(300, 2400),
  cw(300, 700), cwn(470, 530),
  nbfm(-6000, 6000), nnfm(-3000, 3000),
  iq(-5000, 5000);

  public final int lf;
  public final int hf;

  TkModulation(int lf, int hf) {
    this.lf = lf;
    this.hf = hf;
  }
}
