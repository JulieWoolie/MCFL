package me.jules.mcfl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.google.common.io.CharSource;
import com.google.common.io.Resources;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import me.jules.mcfl.debug.DebugUtil;
import me.jules.mcfl.interpreter.ReturnValue;
import me.jules.mcfl.interpreter.Scope;

public final class TestUtils {

  static ReturnValue evaluateResource(String resourceName) {
    return evaluateResource(resourceName, null);
  }

  static ReturnValue evaluateResource(String resourceName, Consumer<Scope> scopeConsumer) {
    URL url = Resources.getResource(resourceName);
    CharSource src = Resources.asCharSource(url, StandardCharsets.UTF_8);

    String str = assertDoesNotThrow(src::read);
    return evaluateString(str, scopeConsumer);
  }

  static ReturnValue evaluateString(String str, Consumer<Scope> scopeConsumer) {
    StringBuffer buf = new StringBuffer(str);
    return DebugUtil.evaluate(buf, System.out, scopeConsumer);
  }
}
