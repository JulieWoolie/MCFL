package me.jules.mcfl.ast;

import lombok.Getter;
import lombok.Setter;
import me.jules.mcfl.interpreter.ExecContext;
import me.jules.mcfl.interpreter.ReturnValue;
import me.jules.mcfl.interpreter.Scope;

@Getter @Setter
public class BooleanLiteral extends Expression {

  private boolean value;

  @Override
  public ReturnValue execute(ExecContext ctx, Scope scope) {
    return ReturnValue.directWrap(value);
  }

  @Override
  public <R, C> R visit(NodeVisitor<R, C> visitor, C context) {
    return visitor.visitBoolean(this, context);
  }
}
