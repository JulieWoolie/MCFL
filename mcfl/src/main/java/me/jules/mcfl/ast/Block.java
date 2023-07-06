package me.jules.mcfl.ast;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
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

  @Setter @Getter
  private boolean loopBlock = false;

  @Override
  public ReturnValue execute(ExecContext ctx, Scope scope) throws EvaluationError {
    return executeSameScope(ctx, scope.newChild());
  }

  public ReturnValue executeSameScope(ExecContext ctx, Scope scope) throws EvaluationError {
    functions.forEach(functionStatement -> functionStatement.execute(ctx, scope));

    for (Statement n : body) {
      ReturnValue res = n.execute(ctx, scope);

      if (res.kind() == Kind.LOOP_BREAK || res.kind() == Kind.LOOP_CONTINUE) {
        if (loopBlock) {
          return res;
        }

        continue;
      }

      if (res.kind() != Kind.EMPTY) {
        return res;
      }
    }

    return ReturnValue.NO_RETURN;
  }

  @Override
  public <R, C> R visit(NodeVisitor<R, C> visitor, C context) {
    return visitor.visitBlock(this, context);
  }
}
