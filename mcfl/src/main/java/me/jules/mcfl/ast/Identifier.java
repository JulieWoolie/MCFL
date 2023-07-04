package me.jules.mcfl.ast;

import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import me.jules.mcfl.EvaluationError;
import me.jules.mcfl.interpreter.ExecContext;
import me.jules.mcfl.interpreter.ReturnValue;
import me.jules.mcfl.interpreter.Scope;

public class Identifier extends Expression {

  @Getter
  private String value;

  public void setValue(String value) {
    Objects.requireNonNull(value);
    this.value = value;
  }

  @Override
  public ReturnValue execute(ExecContext ctx, Scope scope) throws EvaluationError {
    if (!scope.containsValue(value)) {
      throw new EvaluationError("No binding named '" + value + "'", getPosition());
    }

    return ReturnValue.wrap(scope.getProperty(this.value));
  }

  @Override
  public <R, C> R visit(NodeVisitor<R, C> visitor, C context) {
    return visitor.visitIdentifier(this, context);
  }

  @Override
  public String toString() {
    return value;
  }
}
