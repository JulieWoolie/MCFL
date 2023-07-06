package me.jules.mcfl.ast;

import lombok.Getter;
import lombok.Setter;
import me.jules.mcfl.EvaluationError;
import me.jules.mcfl.interpreter.Bindings;
import me.jules.mcfl.interpreter.ExecContext;
import me.jules.mcfl.interpreter.ObjectProperty;
import me.jules.mcfl.interpreter.ReturnValue;
import me.jules.mcfl.interpreter.Scope;
import me.jules.mcfl.interpreter.Utils;

@Getter @Setter
public class PropertyAccessExpr extends Expression {

  private Expression target;
  private Identifier propertyName;

  @Override
  public ReturnValue execute(ExecContext context, Scope scope) throws EvaluationError {
    ReturnValue value = target.execute(context, scope);
    Utils.ensureValuePresent(value, target.getPosition(), null);

    Object o = value.ref().value();

    if (!(o instanceof Bindings bindings)) {
      throw new EvaluationError("Did not return an object value", target.getPosition());
    }

    ObjectProperty property = bindings.getProperty(propertyName.getValue());
    return ReturnValue.wrap(property);
  }

  @Override
  public <R, C> R visit(NodeVisitor<R, C> visitor, C context) {
    return visitor.visitPropertyAccess(this, context);
  }
}
