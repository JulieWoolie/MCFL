package me.jules.mcfl.ast;

import me.jules.mcfl.EvaluationError;
import me.jules.mcfl.interpreter.ExecContext;
import me.jules.mcfl.interpreter.ReturnValue;
import me.jules.mcfl.interpreter.Scope;

public class DoWhileStatement extends LoopStatement {

  @Override
  public ReturnValue execute(ExecContext context, Scope scope) throws EvaluationError {
    do {
      ReturnValue bodyResult = body.execute(context, scope);
      int code = analyseResult(bodyResult);

      if (code == RES_RETURN) {
        return bodyResult;
      } else if (code == RES_BREAK) {
        break;
      }
    } while (testCondition(context, scope));

    return ReturnValue.NO_RETURN;
  }

  @Override
  public <R, C> R visit(NodeVisitor<R, C> visitor, C context) {
    return visitor.visitDoWhile(this, context);
  }
}
