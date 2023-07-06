package me.jules.mcfl.ast;

import lombok.Getter;
import lombok.Setter;
import me.jules.mcfl.EvaluationError;
import me.jules.mcfl.interpreter.Bindings;
import me.jules.mcfl.interpreter.ExecContext;
import me.jules.mcfl.interpreter.ReturnValue;
import me.jules.mcfl.interpreter.ReturnValue.Kind;
import me.jules.mcfl.interpreter.Scope;

@Getter @Setter
public class VariableDefinition extends Statement {

  private Type type;
  private Identifier name;
  private Expression value;

  private boolean alreadyDefined = false;

  @Override
  public ReturnValue execute(ExecContext ctx, Scope scope) throws EvaluationError {
    if (!scope.canOverride(name.getValue())) {
      if (alreadyDefined) {
        return ReturnValue.NO_RETURN;
      }

      throw new EvaluationError("Cannot redefine variable '" + name + "'", getPosition());
    }

    ReturnValue varValue;

    if (this.value == null) {
      varValue = ReturnValue.NO_RETURN;
    } else {
      varValue = value.execute(ctx, scope);

      if (varValue.kind() != Kind.RETURN_VALUE && varValue.kind() != Kind.RETURN_NONE) {
        throw new EvaluationError("Invalid return value", value.getPosition());
      }
    }

    type.define(scope, ctx, name.getValue(), varValue.ref().value());
    alreadyDefined = true;

    return ReturnValue.NO_RETURN;
  }

  @Override
  public <R, C> R visit(NodeVisitor<R, C> visitor, C context) {
    return visitor.visitVarDefinition(this, context);
  }

  public enum Type {

    GLOBAL {
      @Override
      void define(Scope scope, ExecContext ctx, String name, Object value) {
        ctx.getGlobalScope().defineValue(name, value, 0);
      }
    },

    REGULAR {
      @Override
      void define(Scope scope, ExecContext ctx, String name, Object value) {
        scope.defineValue(name, value, 0);
      }
    },

    CONST {
      @Override
      void define(Scope scope, ExecContext ctx, String name, Object value) {
        scope.defineValue(name, value, Bindings.FLAG_CONST);
      }
    };

    abstract void define(Scope scope, ExecContext ctx, String name, Object value);
  }
}
