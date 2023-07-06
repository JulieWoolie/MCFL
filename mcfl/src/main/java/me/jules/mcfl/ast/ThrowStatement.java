package me.jules.mcfl.ast;

import lombok.Getter;
import lombok.Setter;
import me.jules.mcfl.EvaluationError;
import me.jules.mcfl.interpreter.ExecContext;
import me.jules.mcfl.interpreter.ReturnValue;
import me.jules.mcfl.interpreter.Scope;

@Getter @Setter
public class ThrowStatement extends Statement {

  private Expression message;

  @Override
  public ReturnValue execute(ExecContext context, Scope scope) throws EvaluationError {
    if (message == null) {
      throw new EvaluationError("Throw statement reached");
    }

    ReturnValue val = message.execute(context, scope);
    String str = val.ref().toString();
    throw new EvaluationError(str, getPosition());
  }

  @Override
  public <R, C> R visit(NodeVisitor<R, C> visitor, C context) {
    return visitor.visitThrow(this, context);
  }
}
