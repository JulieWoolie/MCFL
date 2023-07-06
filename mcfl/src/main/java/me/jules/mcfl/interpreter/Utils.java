package me.jules.mcfl.interpreter;

import java.util.Objects;
import java.util.StringJoiner;
import me.jules.mcfl.EvaluationError;
import me.jules.mcfl.Location;
import me.jules.mcfl.interpreter.ReturnValue.Kind;

public final class Utils {

  public static Boolean getBoolean(Object o) {
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

    return null;
  }

  public static void ensureValuePresent(ReturnValue value, Location location, String extra)
      throws EvaluationError
  {
    if (value.kind() == Kind.RETURN_VALUE) {
      return;
    }

    if (extra == null) {
      throw new EvaluationError("No return value", location);
    } else {
      throw new EvaluationError("No return value, " + extra, location);
    }
  }

  public static void ensureParamCount(ValueRef[] params, int required)
      throws EvaluationError
  {
    if (params.length != required) {
      throw new EvaluationError(
          "Invalid number of arguments, requires %s, found %s"
              .formatted(required, params.length)
      );
    }
  }

  public static Number getNumber(Object o) throws EvaluationError {
    if (o instanceof ValueRef v) {
      return getNumber(v.value());
    }

    if (o instanceof Number number) {
      return number;
    }

    if (o instanceof Boolean bool) {
      return bool ? 1 : 0;
    }

    if (o instanceof String str && !str.isEmpty()) {
      try {
        return parseNumber(str);
      } catch (NumberFormatException exc) {
        // 'throw' up ahead will handle this scenario
      }
    }

    throw new EvaluationError("Value '" + o + "' cannot be converted to number");
  }

  private static Number parseNumber(String str) throws NumberFormatException {
    int len = str.length();

    if (len < 3) {
      return Double.parseDouble(str);
    }

    char first = str.charAt(0);
    int sign;

    if (first == '+') {
      sign = 1;
      str = str.substring(1);
      len = str.length();
    } else if (first == '-') {
      sign = -1;
      str = str.substring(1);
      len = str.length();
    } else {
      sign = 1;
    }

    if (first == '0') {
      char second = str.charAt(1);
      String sub = str.substring(2);

      if (second == 'x' || second == 'X') {
        return sign * Long.parseUnsignedLong(sub, 16);
      }

      if (second == 'b' || second == 'B') {
        return sign * Long.parseUnsignedLong(sub, 2);
      }

      if (second == 'o' || second == 'O') {
        return sign * Long.parseUnsignedLong(sub, 8);
      }
    }

    return sign * Double.parseDouble(str);
  }

  public static String numberToString(Number number) {
    double n = number.doubleValue();
    long l = number.longValue();

    if (n == l) {
      return String.valueOf(l);
    }

    return String.valueOf(n);
  }

  public static String toString(ValueRef[] params) {
    StringJoiner joiner = new StringJoiner("");

    for (Object o: params) {
      joiner.add(Objects.toString(o));
    }

    return joiner.toString();
  }

  public static boolean equals(Object o1, Object o2) {
    if (o1 == null) {
      return o2 == null;
    }

    if (o2 == null) {
      return false;
    }

    if (o1 instanceof Number num1 && o2 instanceof Number num2) {
      return num1.doubleValue() == num2.doubleValue();
    }

    return Objects.deepEquals(o1, o2);
  }
}
