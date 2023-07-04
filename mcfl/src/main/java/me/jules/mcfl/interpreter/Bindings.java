package me.jules.mcfl.interpreter;

import java.util.Map.Entry;
import java.util.Set;

public interface Bindings {

  int FLAG_UNINITIALIZED  = 0x1;
  int FLAG_CONST          = 0x2;

  boolean containsValue(String name);

  void defineValue(String name, Object value, int flags);

  ObjectProperty getProperty(String name);

  void setProperty(String name, ObjectProperty property);

  Object get(String name);

  void put(String name, Object value);

  void putConst(String name, Object value);

  Set<String> keys();

  Set<Entry<String, Object>> values();
}
