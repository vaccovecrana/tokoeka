package io.vacco.tokoeka.audio;

public class TkAdpcm {

  private static final int[] STEP_SIZE_TABLE = {
      7, 8, 9, 10, 11, 12, 13, 14, 16, 17,
      19, 21, 23, 25, 28, 31, 34, 37, 41, 45,
      50, 55, 60, 66, 73, 80, 88, 97, 107, 118,
      130, 143, 157, 173, 190, 209, 230, 253, 279, 307,
      337, 371, 408, 449, 494, 544, 598, 658, 724, 796,
      876, 963, 1060, 1166, 1282, 1411, 1552, 1707, 1878, 2066,
      2272, 2499, 2749, 3024, 3327, 3660, 4026, 4428, 4871, 5358,
      5894, 6484, 7132, 7845, 8630, 9493, 10442, 11487, 12635, 13899,
      15289, 16818, 18500, 20350, 22385, 24623, 27086, 29794, 32767
  };

  private static final int[] INDEX_TABLE = {
      -1, -1, -1, -1, 2, 4, 6, 8,
      -1, -1, -1, -1, 2, 4, 6, 8
  };

  private int predicted;
  private int index;

  public TkAdpcm() {
    this.predicted = 0;
    this.index = 0;
  }

  public byte[] decode(byte[] adpcm) {
    byte[] pcm = new byte[adpcm.length * 4]; // Each byte of ADPCM decodes to two bytes of PCM
    int step;
    int diff;
    int pcmIndex = 0;

    for (int input : adpcm) {
      for (int j = 0; j < 2; j++) { // Process each nibble
        int nibble = j == 0 ? (input & 0x0f) : ((input >> 4) & 0x0f);
        step = STEP_SIZE_TABLE[this.index];
        diff = step >> 3;

        if ((nibble & 4) != 0) diff += step;
        if ((nibble & 2) != 0) diff += step >> 1;
        if ((nibble & 1) != 0) diff += step >> 2;
        if ((nibble & 8) != 0) this.predicted -= diff;
        else this.predicted += diff;
        if (this.predicted > 32767) this.predicted = 32767;
        else if (this.predicted < -32768) this.predicted = -32768;
        this.index += INDEX_TABLE[nibble];
        if (this.index < 0) this.index = 0;
        if (this.index > 88) this.index = 88;

        pcm[pcmIndex++] = (byte) (this.predicted & 0xff);
        pcm[pcmIndex++] = (byte) ((this.predicted >> 8) & 0xff);
      }
    }

    return pcm;
  }
}

