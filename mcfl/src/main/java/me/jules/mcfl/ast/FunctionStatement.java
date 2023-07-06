package me.jules.mcfl.ast;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import me.jules.mcfl.EvaluationError;
import me.jules.mcfl.ScriptCallable;
import me.jules.mcfl.interpreter.Bindings;
import me.jules.mcfl.interpreter.ExecContext;
import me.jules.mcfl.interpreter.Utils;
import me.jules.mcfl.interpreter.ReturnValue;
import me.jules.mcfl.interpreter.Scope;
import me.jules.mcfl.interpreter.ValueRef;

@Getter @Setter
public class FunctionStatement extends Statement implements ScriptCallable {

  private Identifier name;

  private final List<Identifier> parameters = new ArrayList<>();

  private Block body;

  @Override
  public ReturnValue execute(ExecContext ctx, Scope scope) {
    scope.defineValue(name.getValue(), this, Bindings.FLAG_CONST);
    return ReturnValue.NO_RETURN;
  }

  @Override
  public <R, C> R visit(NodeVisitor<R, C> visitor, C context) {
    return visitor.visitFunction(this, context);
  }

  @Override
  public ReturnValue call(ExecContext ctx, Scope scope, ValueRef[] params)
      throws EvaluationError
  {
    Utils.ensureParamCount(params, parameters.size());
    Scope newScope = scope.newChild();

    for (int i = 0; i < parameters.size(); i++) {
      Object value = params[i];
      Identifier name = parameters.get(i);

      newScope.defineValue(name.getValue(), value, 0);
    }

    return body.execute(ctx, newScope);
  }
}
