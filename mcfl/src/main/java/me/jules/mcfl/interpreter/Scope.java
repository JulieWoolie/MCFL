package me.jules.mcfl.interpreter;

import java.util.Collections;
import java.util.Map.Entry;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class Scope extends BindingObject implements Bindings {

  private Scope parent;

  public Scope getRootScope() {
    Scope result = this;

    while (result.parent != null) {
      result = result.parent;
    }

    return result;
  }

  @Override
  protected boolean canOverride(String name) {
    if (!super.canOverride(name)) {
      return false;
    }

    if (parent == null) {
      return true;
    }

    return parent.canOverride(name);
  }

  @Override
  public boolean containsValue(String name) {
    return super.containsValue(name)
        || (parent != null && parent.containsValue(name));
  }

  @Override
  public ObjectProperty getProperty(String name) {
    if (parent != null) {
      var prop = parent.getProperty(name);

      if (prop != null) {
        return prop;
      }
    }

    return super.getProperty(name);
  }

  @Override
  public Object get(String name) {
    if (parent != null) {
      var val = parent.get(name);

      if (val != null) {
        return val;
      }
    }

    return super.get(name);
  }

  public Scope newChild() {
    Scope child = new Scope();
    child.parent = this;
    return child;
  }

  @Override
  public Set<String> keys() {
    if (parent != null) {
      Set<String> parentResult = parent.keys();
      parentResult.addAll(propertyMap.keySet());
      return Collections.unmodifiableSet(parentResult);
    }

    return super.keys();
  }

  @Override
  public Set<Entry<String, Object>> values() {
    if (parent != null) {
      var parentSet = parent.values();
      parentSet.addAll(super.values());
      return parentSet;
    }

    return super.values();
  }
}
