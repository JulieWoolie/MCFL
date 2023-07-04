package me.jules.mcfl.ast;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import me.jules.mcfl.EvaluationError;
import me.jules.mcfl.interpreter.ExecContext;
import me.jules.mcfl.interpreter.ReturnValue;
import me.jules.mcfl.interpreter.ReturnValue.Kind;
import me.jules.mcfl.interpreter.Scope;

public class Block extends Statement {

  @Getter
  final List<Statement> body = new ArrayList<>();

  @Getter
  final List<FunctionStatement> functions = new ArrayList<>();

  @Override
  public ReturnValue execute(ExecContext ctx, Scope scope) throws EvaluationError {
    Scope nScope = createChild(scope);
    functions.forEach(functionStatement -> functionStatement.execute(ctx, nScope));

    for (Statement n : body) {
      ReturnValue res = n.execute(ctx, nScope);

      if (res.kind() == Kind.RETURN_VALUE || res.kind() == Kind.RETURN_NONE) {
        return res;
      }
    }

    return ReturnValue.NO_RETURN;
  }

  protected Scope createChild(Scope scope) {
    return scope.newChild();
  }
  @Override
  public <R, C> R visit(NodeVisitor<R, C> visitor, C context) {
    return visitor.visitBlock(this, context);
  }
}
