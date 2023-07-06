package me.jules.mcfl.ast;

import lombok.Getter;
import lombok.Setter;
import me.jules.mcfl.EvaluationError;
import me.jules.mcfl.interpreter.ExecContext;
import me.jules.mcfl.interpreter.ReturnValue;
import me.jules.mcfl.interpreter.ReturnValue.Kind;
import me.jules.mcfl.interpreter.Scope;
import me.jules.mcfl.interpreter.Utils;
import me.jules.mcfl.interpreter.ValueRef;

@Getter @Setter
public abstract class LoopStatement extends Statement {

  protected static final int RES_CONTINUE = 0;
  protected static final int RES_RETURN   = 1;
  protected static final int RES_BREAK    = 2;

  protected Identifier label;

  protected Expression condition;
  protected Block body;

  protected boolean testCondition(ExecContext context, Scope scope) throws EvaluationError {
    if (condition == null) {
      return true;
    }

    ReturnValue value = condition.execute(context, scope);
    Utils.ensureValuePresent(value, condition.getPosition(), "Condition did not return a value");

    return value.ref().getBoolean();
  }

  protected static String codeToResult(int code) {
    return switch (code) {
      case RES_CONTINUE -> "continue";
      case RES_RETURN -> "return";
      default -> "break";
    };
  }

  protected int analyseResult(ReturnValue bodyResult) {
    Kind kind = bodyResult.kind();
    ValueRef ref = bodyResult.ref();
    String valueLabel;

    if (ref == null) {
      valueLabel = null;
    } else {
      valueLabel = ref.getString();
    }

    return switch (kind) {
      case RETURN_NONE, RETURN_VALUE -> RES_RETURN;
      case LOOP_BREAK -> fromLabelTest(valueLabel, RES_BREAK);
      case LOOP_CONTINUE -> fromLabelTest(valueLabel, RES_CONTINUE);
      default -> RES_CONTINUE;
    };
  }

  private int fromLabelTest(String valueLabel, int breakCode) {
    if (valueLabel == null || valueLabel.isEmpty()) {
      return breakCode;
    }

    if (label == null) {
      return RES_RETURN;
    }

    if (!label.getValue().equals(valueLabel)) {
      return RES_RETURN;
    }

    return breakCode;
  }
}
