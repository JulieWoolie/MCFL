package me.jules.mcfl.ast;

import lombok.Getter;
import lombok.Setter;
import me.jules.mcfl.EvaluationError;
import me.jules.mcfl.interpreter.ExecContext;
import me.jules.mcfl.interpreter.ReturnValue;
import me.jules.mcfl.interpreter.ReturnValue.Kind;
import me.jules.mcfl.interpreter.Scope;

@Getter @Setter
public class LoopFlowStatement extends Statement {

  private Type type;
  private Identifier label;

  @Override
  public ReturnValue execute(ExecContext context, Scope scope) throws EvaluationError {
    String stringLabel = label == null ? null : label.getValue();

    if (type == Type.BREAK) {
      return ReturnValue.breakValue(stringLabel);
    } else {
      return ReturnValue.continueValue(stringLabel);
    }
  }

  @Override
  public <R, C> R visit(NodeVisitor<R, C> visitor, C context) {
    return visitor.visitLoopFlow(this, context);
  }

  public enum Type {
    BREAK, CONTINUE
  }
}
