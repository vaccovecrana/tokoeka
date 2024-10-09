package io.vacco.tokoeka;

import io.vacco.tokoeka.util.TkFormat;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;

import static io.vacco.tokoeka.util.TkPair.*;
import static io.vacco.tokoeka.util.TkCommand.*;
import static j8spec.J8Spec.*;
import static org.junit.Assert.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class TkPairTest {
  static {
    it("Generates command pairs", () -> {
      var cmd = set(
        ks("single", null),
        ks("hello", "world"),
        kb("boolFlag1", true),
        kb("boolFlag0", false),
        ki("intArg", 999),
        kd3("dblArg", 1.2345)
      );
      assertEquals("SET single hello=world boolFlag1=1 boolFlag0=0 intArg=999 dblArg=1.235", cmd);
    });
    it("Formats redacted messages", () -> {
      assertEquals("null", TkFormat.shorten(null, 0));
      assertEquals("1", TkFormat.shorten("1", 2));
      assertEquals("1", TkFormat.shorten("1", 1));
      assertEquals("1", TkFormat.shorten("1", 2));
      assertEquals("1...1", TkFormat.shorten("111", 2));
      assertEquals("1...1", TkFormat.shorten("1111", 2));
      assertEquals("1...1", TkFormat.shorten("11111", 2));
      assertEquals("111", TkFormat.shorten("111", 3));
      assertEquals("1...1", TkFormat.shorten("1111", 3));
      assertEquals("1...1", TkFormat.shorten("11111", 3));
      assertEquals("01...34", TkFormat.shorten("01234", 4));
      assertEquals("0123...CDEF", TkFormat.shorten("0123456789ABCDEF", 8));
    });
  }
}
