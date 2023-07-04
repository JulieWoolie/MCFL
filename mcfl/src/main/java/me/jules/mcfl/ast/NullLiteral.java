package me.jules.mcfl.ast;

import me.jules.mcfl.interpreter.ExecContext;
import me.jules.mcfl.interpreter.ReturnValue;
import me.jules.mcfl.interpreter.Scope;

public class NullLiteral extends Expression {

  @Override
  public ReturnValue execute(ExecContext ctx, Scope scope) {
    return ReturnValue.directWrap(null);
  }

  @Override
  public <R, C> R visit(NodeVisitor<R, C> visitor, C context) {
    return visitor.visitNull(this, context);
  }
}
