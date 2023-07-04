package me.jules.mcfl.ast;

import lombok.Getter;
import lombok.Setter;
import me.jules.mcfl.interpreter.ExecContext;
import me.jules.mcfl.interpreter.ReturnValue;
import me.jules.mcfl.interpreter.Scope;

@Getter @Setter
public class NumberLiteral extends Expression {

  private Number number;

  @Override
  public ReturnValue execute(ExecContext ctx, Scope scope) {
    return ReturnValue.directWrap(number);
  }

  @Override
  public <R, C> R visit(NodeVisitor<R, C> visitor, C context) {
    return visitor.visitNumber(this, context);
  }
}
