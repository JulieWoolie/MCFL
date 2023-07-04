package me.jules.mcfl.ast;

import lombok.Getter;
import lombok.Setter;
import me.jules.mcfl.EvaluationError;
import me.jules.mcfl.interpreter.ExecContext;
import me.jules.mcfl.interpreter.ReturnValue;
import me.jules.mcfl.interpreter.Scope;

@Getter @Setter
public class ReturnStatement extends Statement {

  private Expression value;

  @Override
  public ReturnValue execute(ExecContext context, Scope scope) throws EvaluationError {
    if (value == null) {
      return ReturnValue.VOID;
    }

    return value.execute(context, scope);
  }

  @Override
  public <R, C> R visit(NodeVisitor<R, C> visitor, C context) {
    return visitor.visitReturn(this, context);
  }
}
