package me.jules.mcfl;

import java.util.Objects;
import me.jules.mcfl.interpreter.ObjectProperty;

class SystemTimeProperty implements ObjectProperty {

  @Override
  public Object value() {
    return System.currentTimeMillis();
  }

  @Override
  public void set(Object value) {
    throw new UnsupportedOperationException("System time cannot be reassigned");
  }

  @Override
  public String toString() {
    return Objects.toString(value());
  }
}
