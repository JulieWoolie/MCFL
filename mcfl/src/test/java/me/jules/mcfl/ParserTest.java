package me.jules.mcfl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.google.common.io.CharSource;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;
import me.jules.mcfl.ast.FunctionFile;
import me.jules.mcfl.interpreter.ExecContext;
import me.jules.mcfl.interpreter.ReturnValue;
import me.jules.mcfl.interpreter.Scope;
import org.junit.jupiter.api.Test;

public class ParserTest {

  @Test
  void main() {
    URL url = Resources.getResource("test_script.mcfl");
    CharSource src = Resources.asCharSource(url, StandardCharsets.UTF_8);
    StringBuffer buf = new StringBuffer(assertDoesNotThrow(src::read));

    FunctionFile file = FunctionLang.parse(buf);

    ExecContext ctx = new ExecContext();
    ctx.setExecutor(command -> {
      System.out.printf("command executed='%s'\n", command );
      return 0;
    });

    Scope scope = FunctionLang.defineStandardScope();

    try {
      file.execute(ctx, scope);
    } catch (EvaluationError err) {
      Location l = err.getScriptLocation();

      if (l == null) {
        err.printStackTrace();
        return;
      }

      String message = ErrorMessages.format(buf, l, err.getBaseMessage());
      throw new RuntimeException(message, err);
    }
  }
}
