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
    describe("Websocket commands", () -> {
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
    });
  }
}
