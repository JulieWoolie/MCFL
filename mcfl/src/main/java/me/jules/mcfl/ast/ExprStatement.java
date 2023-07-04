package me.jules.mcfl.ast;

import lombok.Getter;
import lombok.Setter;
import me.jules.mcfl.EvaluationError;
import me.jules.mcfl.interpreter.ExecContext;
import me.jules.mcfl.interpreter.ReturnValue;
import me.jules.mcfl.interpreter.Scope;

@Getter @Setter
public class ExprStatement extends Statement {

  private Expression expr;

  @Override
  public ReturnValue execute(ExecContext context, Scope scope) throws EvaluationError {
    expr.execute(context, scope);
    return ReturnValue.NO_RETURN;
  }

  @Override
  public <R, C> R visit(NodeVisitor<R, C> visitor, C context) {
    return visitor.visitExprStatement(this, context);
  }
}
