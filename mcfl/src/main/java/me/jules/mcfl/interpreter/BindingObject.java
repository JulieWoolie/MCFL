package me.jules.mcfl.interpreter;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import me.jules.mcfl.interpreter.ObjectProperty.RegularProperty;

public class BindingObject implements Bindings {

  final Map<String, ObjectProperty> propertyMap = new HashMap<>();

  @Override
  public boolean containsValue(String name) {
    return propertyMap.containsKey(name);
  }

  public ObjectProperty getProperty(String name) {
    return propertyMap.get(name);
  }

  public void setProperty(String name, ObjectProperty property) {
    if (containsValue(name)) {
      throw new RuntimeException("Cannot redefine property: '" + name + "'");
    }

    propertyMap.put(name, property);
  }

  protected boolean canOverride(String name) {
    return !containsValue(name);
  }

  @Override
  public void defineValue(String name, Object value, int flags) {
    if (!canOverride(name)) {
      throw new RuntimeException("Duplicate definition of '" + name + "'");
    }

    RegularProperty property = new RegularProperty();
    property.flags = flags;
    property.value = value;
    setProperty(name, property);
  }

  public Object get(String name) {
    ObjectProperty property = getProperty(name);
    return property != null ? property.value() : null;
  }

  public void put(String name, Object value) {
    _put(name, value, 0);
  }

  public void putConst(String name, Object value) {
    _put(name, value, FLAG_CONST);
  }

  private void _put(String name, Object value, int flags) {
    ObjectProperty property = getProperty(name);

    if (property == null) {
      RegularProperty reg = new RegularProperty();
      reg.flags = flags;
      property = reg;

      setProperty(name, property);
    }

    property.set(value);
  }

  @Override
  public Set<String> keys() {
    return Collections.unmodifiableSet(propertyMap.keySet());
  }

  @Override
  public Set<Entry<String, Object>> values() {
    Set<Entry<String, Object>> entries = new HashSet<>(propertyMap.size());
    for (var e: propertyMap.entrySet()) {
      Entry<String, Object> entry = new PropertyEntry(e.getKey(), e.getValue());
      entries.add(entry);
    }
    return entries;
  }

  public static class PropertyEntry implements Entry<String, Object> {

    private final String key;
    private final ObjectProperty property;

    public PropertyEntry(String key, ObjectProperty property) {
      this.key = key;
      this.property = property;
    }

    @Override
    public String getKey() {
      return key;
    }

    @Override
    public Object getValue() {
      return property.value();
    }

    @Override
    public Object setValue(Object value) {
      var val = getValue();
      property.set(value);
      return val;
    }

    @Override
    public String toString() {
      return getKey() + "=" + getValue();
    }
  }
}
