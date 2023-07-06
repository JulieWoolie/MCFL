package me.jules.mcfl.ast;

import lombok.Getter;
import lombok.Setter;
import me.jules.mcfl.EvaluationError;
import me.jules.mcfl.interpreter.DebugHandler;
import me.jules.mcfl.interpreter.ExecContext;
import me.jules.mcfl.interpreter.ReturnValue;
import me.jules.mcfl.interpreter.Scope;

@Getter @Setter
public class DebuggerStatement extends Statement {

  private Expression expression;

  @Override
  public ReturnValue execute(ExecContext context, Scope scope) throws EvaluationError {
    if (context.getDebugHandler() == null) {
      return ReturnValue.NO_RETURN;
    }

    ReturnValue exprValue = expression.execute(context, scope);
    DebugHandler handler = context.getDebugHandler();

    handler.debugger(exprValue.ref().getString(), getPosition(), context);
    return ReturnValue.NO_RETURN;
  }

  @Override
  public <R, C> R visit(NodeVisitor<R, C> visitor, C context) {
    return visitor.visitDebugger(this, context);
  }
}
