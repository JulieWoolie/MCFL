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
    Object o = value();
    Boolean b = Utils.getBoolean(o);

    if (b == null) {
      return o != null;
    } else {
      return b;
    }
  }

  default double getDouble() throws EvaluationError {
    return Utils.getNumber(value()).doubleValue();
  }

  default long getLong() throws EvaluationError {
    return Utils.getNumber(value()).longValue();
  }

  default String getString() {
    Object v = value();
    if (v instanceof Number number) {
      return Utils.numberToString(number);
    }
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

  default boolean isSameType(ValueRef other) {
    Object value = value();
    Object oValue = other.value();

    if (value == null) {
      return oValue == null;
    }

    Class c = value.getClass();
    return c.isInstance(oValue);
  }

  default int operatorCompare(ValueRef o) throws EvaluationError {
    if (isSameType(o)) {
      Object vThis = value();
      Object vOther = o.value();

      if (vThis instanceof Comparable c) {
        return c.compareTo(vOther);
      }
    }

    if (operatorEquals(o)) {
      return 0;
    }

    throw new EvaluationError(
        "Don't know how to compare '" + getTypeName() + "' and '" + o.getTypeName() + "'"
    );
  }

  default String getTypeName() {
    Object v = value();

    if (v == null) {
      return "null";
    }

    return v.getClass().getSimpleName();
  }

  default ValueRef operatorLeftShift(ValueRef value) throws EvaluationError {
    return direct(getLong() << value.getLong());
  }

  default ValueRef operatorRightShift(ValueRef value) throws EvaluationError {
    return direct(getLong() >> value.getLong());
  }

  default ValueRef operatorUnsignedRightShift(ValueRef value) throws EvaluationError {
    return direct(getLong() >>> value.getLong());
  }

  default ValueRef operatorUnsignedLeftShift(ValueRef value) throws EvaluationError {
    return direct(getLong() << value.getLong());
  }

  default ValueRef operatorMul(ValueRef value) throws EvaluationError {
    return direct(getDouble() * value.getDouble());
  }

  default ValueRef operatorPow(ValueRef value) throws EvaluationError {
    return direct(Math.pow(getDouble(),  value.getDouble()));
  }

  default ValueRef operatorDiv(ValueRef value) throws EvaluationError {
    return direct(getDouble() / value.getDouble());
  }

  default ValueRef operatorAdd(ValueRef value) throws EvaluationError {
    try {
      return direct(getDouble() + value.getDouble());
    } catch (EvaluationError err) {
      return direct(getString() + value.getString());
    }
  }

  default ValueRef operatorSub(ValueRef value) throws EvaluationError {
    return direct(getDouble() - value.getDouble());
  }

  default ValueRef operatorModulo(ValueRef value) throws EvaluationError {
    return direct(getDouble() % value.getDouble());
  }

  default ValueRef operatorOr(ValueRef value) throws EvaluationError {
    try {
      return direct(getLong() | value.getLong());
    } catch (EvaluationError ignored) {
      return direct(getBoolean() || value.getBoolean());
    }
  }

  default ValueRef operatorXor(ValueRef value) throws EvaluationError {
    try {
      return direct(getLong() ^ value.getLong());
    } catch (EvaluationError ignored) {
      return direct(getBoolean() ^ value.getBoolean());
    }
  }

  default ValueRef operatorAnd(ValueRef value) throws EvaluationError {
    try {
      return direct(getLong() & value.getLong());
    } catch (EvaluationError ignored) {
      return direct(getBoolean() && value.getBoolean());
    }
  }

  default ValueRef operatorNegate() throws EvaluationError {
    return direct(!getBoolean());
  }

  default ValueRef operatorInvert() throws EvaluationError {
    return direct(~getLong());
  }

  default ValueRef operatorPostIncrement() throws EvaluationError{
    double d = getDouble();
    set(d + 1);
    return direct(d);
  }

  default ValueRef operatorPreIncrement() throws EvaluationError {
    double d = getDouble() + 1;
    set(d);
    return this;
  }

  default ValueRef operatorPostDecrement() throws EvaluationError {
    double d = getDouble();
    set(d + 1);
    return direct(d);
  }

  default ValueRef operatorPreDecrement() throws EvaluationError {
    double d = getDouble() + 1;
    set(d);
    return this;
  }

  default ValueRef operatorPositive() throws EvaluationError {
    return direct(+getDouble());
  }

  default ValueRef operatorNegative() throws EvaluationError {
    return direct(-getDouble());
  }

  default boolean operatorEquals(ValueRef po) throws EvaluationError {
    Object thisVal = value();
    Object otherVal = po.value();
    return Utils.equals(thisVal, otherVal);
  }

  class Direct implements ValueRef {

    private final Object value;

    public Direct(Object value) {
      this.value = value;

      if (value instanceof ValueRef ref) {
        throw new RuntimeException("Cannot set value for valueref to be a value ref");
      }
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
