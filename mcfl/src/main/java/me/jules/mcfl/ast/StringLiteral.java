package me.jules.mcfl.ast;

import lombok.Getter;
import lombok.Setter;
import me.jules.mcfl.EvaluationError;
import me.jules.mcfl.interpreter.ExecContext;
import me.jules.mcfl.interpreter.ReturnValue;
import me.jules.mcfl.interpreter.Scope;

@Getter @Setter
public class StringLiteral extends Expression {

  private TemplatedString string;

  @Override
  public ReturnValue execute(ExecContext ctx, Scope scope) throws EvaluationError {
    return ReturnValue.directWrap(string.execute(ctx, scope));
  }

  @Override
  public <R, C> R visit(NodeVisitor<R, C> visitor, C context) {
    return visitor.visitString(this, context);
  }
}
