package me.jules.mcfl.interpreter;

import java.util.Objects;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class ReturnValue {

  public static final ReturnValue BREAK = new ReturnValue(Kind.LOOP_BREAK, null);
  public static final ReturnValue CONTINUE = new ReturnValue(Kind.LOOP_CONTINUE, null);
  public static final ReturnValue VOID = new ReturnValue(Kind.RETURN_NONE, null);
  public static final ReturnValue NO_RETURN = new ReturnValue(Kind.EMPTY, null);

  private final Kind kind;

  private final ValueRef ref;

  public ReturnValue(Kind kind, ValueRef ref) {
    Objects.requireNonNull(kind);

    this.kind = kind;
    this.ref = ref;
  }

  public static ReturnValue directWrap(Object o) {
    return wrap(ValueRef.direct(o));
  }

  public static ReturnValue wrap(ValueRef o) {
    return new ReturnValue(Kind.RETURN_VALUE, o);
  }

  public static ReturnValue continueValue(String label) {
    if (label == null || label.isEmpty()) {
      return CONTINUE;
    }

    return new ReturnValue(Kind.LOOP_CONTINUE, ValueRef.direct(label));
  }

  public static ReturnValue breakValue(String label) {
    if (label == null || label.isEmpty()) {
      return BREAK;
    }

    return new ReturnValue(Kind.LOOP_BREAK, ValueRef.direct(label));
  }

  public static ReturnValue of(Kind kind, ValueRef value) {
    return new ReturnValue(kind, value);
  }

  @Override
  public String toString() {
    return "ReturnValue{" +
        "kind=" + kind +
        ", ref=" + ref +
        '}';
  }

  public enum Kind {
    EMPTY,
    LOOP_BREAK,
    LOOP_CONTINUE,
    RETURN_NONE,
    RETURN_VALUE;
  }

}
