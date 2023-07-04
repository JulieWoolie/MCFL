package me.jules.mcfl.interpreter;

import static me.jules.mcfl.interpreter.Bindings.FLAG_CONST;
import static me.jules.mcfl.interpreter.Bindings.FLAG_UNINITIALIZED;

import java.util.Objects;

public interface ObjectProperty extends ValueRef {

  void set(Object value);

  class RegularProperty implements ObjectProperty {

    Object value;
    int flags;

    @Override
    public Object value() {
      return value;
    }

    @Override
    public void set(Object value) {
      boolean isConst = (flags & FLAG_CONST) == FLAG_CONST;
      boolean uninitialized = (flags & FLAG_UNINITIALIZED) == FLAG_UNINITIALIZED;

      if (isConst && !uninitialized) {
        throw new RuntimeException("Cannot modify const value");
      }

      this.value = value;
      this.flags = flags & ~FLAG_UNINITIALIZED;
    }

    @Override
    public String toString() {
      return Objects.toString(value);
    }
  }
}
