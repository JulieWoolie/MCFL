package me.jules.mcfl;

import java.util.Objects;
import me.jules.mcfl.interpreter.ObjectProperty;

class ScriptTimeProperty implements ObjectProperty {

  private long activationTime;

  @Override
  public Object value() {
    if (activationTime == 0) {
      return 0.0f;
    }

    long time = System.currentTimeMillis();
    double since = time - activationTime;

    return since / 1000;
  }

  @Override
  public void set(Object value) {
    throw new UnsupportedOperationException("Cannot redefine script time");
  }

  @Override
  public String toString() {
    return Objects.toString(value());
  }
}
