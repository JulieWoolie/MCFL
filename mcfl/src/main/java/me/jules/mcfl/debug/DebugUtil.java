package me.jules.mcfl.debug;

import java.io.PrintStream;
import java.util.function.Consumer;
import me.jules.mcfl.ErrorMessages;
import me.jules.mcfl.EvaluationError;
import me.jules.mcfl.FunctionLang;
import me.jules.mcfl.Location;
import me.jules.mcfl.Parser;
import me.jules.mcfl.ScriptCallable;
import me.jules.mcfl.TokenStream;
import me.jules.mcfl.ast.FunctionFile;
import me.jules.mcfl.interpreter.Bindings;
import me.jules.mcfl.interpreter.ExecContext;
import me.jules.mcfl.interpreter.ReturnValue;
import me.jules.mcfl.interpreter.Scope;
import me.jules.mcfl.interpreter.Utils;
import me.jules.mcfl.interpreter.ValueRef;

public final class DebugUtil {

  private static final ScriptCallable TYPE_OF = (ctx, scope, params) -> {
    Utils.ensureParamCount(params, 1);
    ValueRef first = params[0];

    return ReturnValue.directWrap(
        "RefType=%s ValueType=%s".formatted(
            first.getClass().getSimpleName(),
            first.getTypeName()
        )
    );
  };

  public static ReturnValue evaluate(
      StringBuffer buffer,
      PrintStream output,
      Consumer<Scope> scopeConsumer
  ) {
    TokenStream stream = new TokenStream(buffer);
    Parser parser = new Parser(stream);

    FunctionFile file = parser.parse();
    PrintingVisitor.print(file, output);

    ExecContext context = new ExecContext();
    context.setExecutor(command -> {
      output.printf("command='%s'\n", command);
      return 0;
    });
    context.setDebugHandler((message, location, ctx) -> {
      output.printf("[DEBUG] %5s >> %s\n", location, message);
    });

    Scope scope = FunctionLang.defineStandardScope();
    scope.defineValue("typeof", TYPE_OF, Bindings.FLAG_CONST);

    if (scopeConsumer != null) {
      scopeConsumer.accept(scope);
    }

    try {
      return file.execute(context, scope);
    } catch (EvaluationError err) {
      Location l = err.getScriptLocation();

      if (l == null) {
        throw new RuntimeException(err);
      }

      String message = ErrorMessages.format(buffer, l, err.getBaseMessage());
      throw new RuntimeException(message, err);
    }
  }
}
