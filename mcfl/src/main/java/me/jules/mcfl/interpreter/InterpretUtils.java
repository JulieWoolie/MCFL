package me.jules.mcfl.interpreter;

import me.jules.mcfl.EvaluationError;
import me.jules.mcfl.interpreter.ReturnValue.Kind;

public final class InterpretUtils {

  public static boolean getBoolean(Object o) {
    if (o instanceof ReturnValue re) {
      if (re.kind() != Kind.RETURN_VALUE) {
        return false;
      }

      return getBoolean(re.ref().value());
    }

    if (o instanceof ValueRef v) {
      return getBoolean(v.value());
    }

    if (o instanceof Boolean bo) {
      return bo;
    }

    if (o instanceof Number number) {
      return number.longValue() != 0;
    }

    if (o instanceof String str) {
      if (str.equalsIgnoreCase("true")) {
        return true;
      } else if (str.equalsIgnoreCase("false")) {
        return false;
      }
    }

    return o != null;
  }

  public static void ensureParamCount(Object[] params, int required)
      throws EvaluationError
  {
    if (params.length != required) {
      throw new EvaluationError(
          "Invalid number of arguments, requires %s, found %s"
              .formatted(required, params.length)
      );
    }
  }

  public static double getNumber(Object o) throws EvaluationError {
    if (o instanceof ReturnValue value) {
      if (value.kind() == Kind.RETURN_NONE) {
        return 0.0d;
      }

      if (value.kind() == Kind.RETURN_VALUE) {
        return getNumber(value.ref().value());
      }
    }

    if (o instanceof ValueRef v) {
      return getNumber(v.value());
    }

    if (o instanceof Number number) {
      return number.doubleValue();
    }

    if (o instanceof Boolean bool) {
      return bool ? 1 : 0;
    }

    if (o instanceof String str) {
      try {
        return Double.parseDouble(str);
      } catch (NumberFormatException exc) {
        // 'throw' up ahead will handle this scenario
      }
    }

    throw new EvaluationError("Value " + o + " cannot be converted to number");
  }
}
