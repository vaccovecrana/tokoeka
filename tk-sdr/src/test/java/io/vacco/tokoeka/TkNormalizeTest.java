package io.vacco.tokoeka;

import io.vacco.tokoeka.audio.TkNormalize;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;

import static io.vacco.tokoeka.util.TkTimer.nowMsDiffLt;
import static j8spec.J8Spec.*;
import static org.junit.Assert.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class TkNormalizeTest {
  static {
    it("Measures time differences", () -> {
      var nowMs = System.currentTimeMillis();
      while (nowMsDiffLt(nowMs, 2500)) {
        System.out.println("tick...");
        Thread.sleep(250);
      }
    });
    it("Generates no output from empty input", () -> {
      var tkn = new TkNormalize();
      var normalizedChunks = tkn.close();
      assertFalse(normalizedChunks.hasNext());
    });
    it("Normalizes a single chunk",  () -> {
      var tkn = new TkNormalize();
      var pcmChunk = new byte[] {
        0x00, 0x40, 0x00, (byte) 0xC0, // Short values: 16384, -16384
        0x00, 0x20, 0x00, (byte) 0xE0  // Short values: 8192, -8192
      };
      tkn.update(pcmChunk);
      var normalizedChunks = tkn.close();
      assertTrue(normalizedChunks.hasNext());
      var normalized = normalizedChunks.next();
      var expectedNormalized = new byte[] {
        (byte) 0xFF, 0x7F, 0x01, (byte) 0x80, // Normalized short values: 32767, -32768
        (byte) 0xFF, 0x3F, 0x01, (byte) 0xC0  // Normalized short values: 16382, -16384
      };
      assertArrayEquals(expectedNormalized, normalized);
    });
    it("Normalizes multiple chunks", () -> {
      var tkn = new TkNormalize();
      var pcmChunk1 = new byte[] {
        0x00, 0x40, 0x00, (byte) 0xC0 // Short values: 16384, -16384
      };
      var pcmChunk2 = new byte[] {
        0x00, 0x20, 0x00, (byte) 0xE0 // Short values: 8192, -8192
      };

      tkn.update(pcmChunk1);
      tkn.update(pcmChunk2);

      var normalizedChunks = tkn.close();

      assertTrue(normalizedChunks.hasNext());
      var normalized1 = normalizedChunks.next();
      var expectedNormalized1 = new byte[] {
        (byte) 0xFF, 0x7F, 0x01, (byte) 0x80 // Normalized short values: 32767, -32768
      };
      assertArrayEquals(expectedNormalized1, normalized1);
      assertTrue(normalizedChunks.hasNext());
      var normalized2 = normalizedChunks.next();
      var expectedNormalized2 = new byte[] {
        (byte) 0xFF, 0x3F, 0x01, (byte) 0xC0 // Normalized short values: 16382, -16384
      };
      assertArrayEquals(expectedNormalized2, normalized2);
    });
    it("Normalizes samples with peak values", () -> {
      var tkn = new TkNormalize();
      var pcmChunk = new byte[] {
        (byte) 0xFF, 0x7F, (byte) 0x01, (byte) 0x80 // Short values: 32767, -32767
      };
      tkn.update(pcmChunk);
      var normalizedChunks = tkn.close();
      assertTrue(normalizedChunks.hasNext());
      var normalized = normalizedChunks.next();
      assertArrayEquals(pcmChunk, normalized);
    });
  }
}
