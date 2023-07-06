package me.jules.mcfl.ast;

import lombok.Getter;
import lombok.Setter;
import me.jules.mcfl.EvaluationError;
import me.jules.mcfl.interpreter.ExecContext;
import me.jules.mcfl.interpreter.Utils;
import me.jules.mcfl.interpreter.ReturnValue;
import me.jules.mcfl.interpreter.Scope;
import me.jules.mcfl.interpreter.ValueRef;

@Getter @Setter
public class BinaryExpr extends Expression {

  private Expression lhs;
  private Expression rhs;

  private BinaryOp op;

  @Override
  public ReturnValue execute(ExecContext context, Scope scope) throws EvaluationError {
    ReturnValue lhsResult = lhs.execute(context, scope);
    ReturnValue rhsResult = rhs.execute(context, scope);

    Utils.ensureValuePresent(lhsResult, lhs.getPosition(), null);
    Utils.ensureValuePresent(rhsResult, rhs.getPosition(), null);

    ValueRef leftResult = lhsResult.ref();
    ValueRef rightResult = rhsResult.ref();

    ValueRef result = op.compute(leftResult, rightResult);
    return ReturnValue.wrap(result);
  }

  @Override
  public <R, C> R visit(NodeVisitor<R, C> visitor, C context) {
    return visitor.visitBinary(this, context);
  }
}
