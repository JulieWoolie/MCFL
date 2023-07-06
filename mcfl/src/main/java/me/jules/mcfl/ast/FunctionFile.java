package me.jules.mcfl.ast;

import lombok.Getter;
import me.jules.mcfl.EvaluationError;
import me.jules.mcfl.interpreter.ExecContext;
import me.jules.mcfl.interpreter.ReturnValue;
import me.jules.mcfl.interpreter.Scope;

@Getter
public class FunctionFile extends Block {

  @Override
  public <R, C> R visit(NodeVisitor<R, C> visitor, C context) {
    return visitor.visitRoot(this, context);
  }

  @Override
  public ReturnValue execute(ExecContext ctx, Scope scope) throws EvaluationError {
    return executeSameScope(ctx, scope);
  }
}
