package me.jules.mcfl.ast;

import lombok.Getter;
import lombok.Setter;
import me.jules.mcfl.EvaluationError;
import me.jules.mcfl.interpreter.ExecContext;
import me.jules.mcfl.interpreter.ReturnValue;
import me.jules.mcfl.interpreter.Scope;

@Getter @Setter
public class WhileStatement extends LoopStatement {

  @Override
  public ReturnValue execute(ExecContext context, Scope scope) throws EvaluationError {
    while (testCondition(context, scope)) {
      ReturnValue bodyResult = body.execute(context, scope);
      int code = analyseResult(bodyResult);

      if (code == RES_RETURN) {
        return bodyResult;
      } else if (code == RES_BREAK) {
        break;
      }
    }

    return ReturnValue.NO_RETURN;
  }

  @Override
  public <R, C> R visit(NodeVisitor<R, C> visitor, C context) {
    return visitor.visitWhile(this, context);
  }
}
