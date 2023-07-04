package me.jules.mcfl.ast;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import me.jules.mcfl.EvaluationError;
import me.jules.mcfl.ScriptCallable;
import me.jules.mcfl.interpreter.ExecContext;
import me.jules.mcfl.interpreter.ReturnValue;
import me.jules.mcfl.interpreter.Scope;
import me.jules.mcfl.interpreter.ValueRef;

@Getter @Setter
public class CallExpr extends Expression {

  private Expression target;

  private final List<Expression> arguments = new ArrayList<>();

  @Override
  public ReturnValue execute(ExecContext context, Scope scope) throws EvaluationError {
    ReturnValue func = target.execute(context, scope);
    Object o = func.ref().value();

    if (o == null) {
      throw new EvaluationError("Unknown function", target.getPosition());
    }

    if (!(o instanceof ScriptCallable callable)) {
      throw new EvaluationError("'" + target + "' is not a function", target.getPosition());
    }

    ValueRef[] params = new ValueRef[arguments.size()];
    for (int i = 0; i < params.length; i++) {
      Expression expr = arguments.get(i);
      ReturnValue value = expr.execute(context, scope);
      params[i] = value.ref();
    }

    return callable.call(context, scope, params);
  }

  @Override
  public <R, C> R visit(NodeVisitor<R, C> visitor, C context) {
    return visitor.visitFunctionCall(this, context);
  }
}
