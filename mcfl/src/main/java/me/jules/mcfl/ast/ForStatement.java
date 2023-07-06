package me.jules.mcfl.ast;

import lombok.Getter;
import lombok.Setter;
import me.jules.mcfl.EvaluationError;
import me.jules.mcfl.interpreter.ExecContext;
import me.jules.mcfl.interpreter.ReturnValue;
import me.jules.mcfl.interpreter.Scope;

@Getter @Setter
public class ForStatement extends LoopStatement {

  private VariableDefinition first;
  private Expression third;

  @Override
  public ReturnValue execute(ExecContext context, Scope scope) throws EvaluationError {
    Scope loopScope = scope.newChild();

    if (first != null) {
      first.execute(context, loopScope);
    }

    while (testCondition(context, loopScope)) {
      ReturnValue bodyResult = body.execute(context, loopScope);
      int code = analyseResult(bodyResult);

      if (code == RES_RETURN) {
        return bodyResult;
      } else if (code == RES_BREAK) {
        return ReturnValue.NO_RETURN;
      }

      if (third != null) {
        third.execute(context, loopScope);
      }
    }

    return ReturnValue.NO_RETURN;
  }

  @Override
  public <R, C> R visit(NodeVisitor<R, C> visitor, C context) {
    return visitor.visitFor(this, context);
  }
}
