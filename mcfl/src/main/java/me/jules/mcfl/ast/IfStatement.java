package me.jules.mcfl.ast;

import lombok.Getter;
import lombok.Setter;
import me.jules.mcfl.EvaluationError;
import me.jules.mcfl.interpreter.ExecContext;
import me.jules.mcfl.interpreter.ReturnValue;
import me.jules.mcfl.interpreter.Scope;

@Getter @Setter
public class IfStatement extends Statement {

  private Expression condition;
  private Block body;
  private Statement elseStatement;

  @Override
  public ReturnValue execute(ExecContext ctx, Scope scope) throws EvaluationError {
    ReturnValue conditionValue = condition.execute(ctx, scope);
    boolean val = conditionValue.ref().getBoolean();

    if (val) {
      return body.execute(ctx, scope);
    } else if (elseStatement != null) {
      return elseStatement.execute(ctx, scope);
    }

    return ReturnValue.NO_RETURN;
  }

  @Override
  public <R, C> R visit(NodeVisitor<R, C> visitor, C context) {
    return visitor.visitIf(this, context);
  }
}
