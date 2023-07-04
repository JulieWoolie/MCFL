package me.jules.mcfl.interpreter;

import java.util.Objects;
import me.jules.mcfl.EvaluationError;
import me.jules.mcfl.ScriptCallable;
import me.jules.mcfl.ScriptExecutable;

public interface ValueRef extends ScriptExecutable, ScriptCallable {

  ValueRef TRUE = new Direct(true);
  ValueRef FALSE = new Direct(false);

  ValueRef NULL = new Direct(null);

  ValueRef EMPTY_STRING = new Direct("");

  static ValueRef direct(Object o) {
    if (o instanceof Boolean b) {
      return b ? TRUE : FALSE;
    }

    if (o == null) {
      return NULL;
    }

    if ("".equals(o)) {
      return EMPTY_STRING;
    }

    return new Direct(o);
  }

  Object value();

  void set(Object value);

  default boolean getBoolean() {
    return InterpretUtils.getBoolean(value());
  }

  default double getDouble() throws EvaluationError {
    return InterpretUtils.getNumber(value());
  }

  default String getString() {
    Object v = value();
    return Objects.toString(v);
  }

  @Override
  default ReturnValue execute(ExecContext ctx, Scope scope) throws EvaluationError {
    Object v = value();

    if (!(v instanceof ScriptExecutable exec)) {
      throw new EvaluationError("Value is not callable");
    }

    return exec.execute(ctx, scope);
  }

  @Override
  default ReturnValue call(ExecContext ctx, Scope scope, ValueRef[] params) throws EvaluationError {
    Object v = value();

    if (!(v instanceof ScriptCallable callable)) {
      throw new EvaluationError("Value is not a function");
    }

    return callable.call(ctx, scope, params);
  }

  class Direct implements ValueRef {

    private final Object value;

    public Direct(Object value) {
      this.value = value;
    }

    @Override
    public Object value() {
      return value;
    }

    @Override
    public void set(Object value) {
      throw new UnsupportedOperationException("Cannot reassign direct value");
    }

    @Override
    public String toString() {
      return Objects.toString(value);
    }
  }
}
