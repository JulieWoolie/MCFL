package me.jules.mcfl.ast;

import lombok.Getter;
import lombok.Setter;
import me.jules.mcfl.EvaluationError;
import me.jules.mcfl.interpreter.ExecContext;
import me.jules.mcfl.interpreter.ReturnValue;
import me.jules.mcfl.interpreter.Scope;
import me.jules.mcfl.interpreter.ValueRef;

@Getter @Setter
public class UnaryExpr extends Expression {

  private Expression expression;

  private UnaryOp operation;

  @Override
  public ReturnValue execute(ExecContext context, Scope scope) throws EvaluationError {
    ValueRef ref = expression.execute(context, scope).ref();
    ValueRef result = operation.compute(ref);

    return ReturnValue.wrap(result);
  }

  @Override
  public <R, C> R visit(NodeVisitor<R, C> visitor, C context) {
    return visitor.visitUnary(this, context);
  }
}
