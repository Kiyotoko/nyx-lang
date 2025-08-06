package org.nyx;

import java.util.HashMap;
import java.util.Map;

public class Environment {
  private final Map<String, Object> values = new HashMap<>();

  public void declare(Token name, Object value) {
    if (!values.containsKey(name.lexeme)) values.put(name.lexeme, value);
    else Nyx.error(name.line, "Variable '" + name.lexeme + "' is already declared.");
  }

  public void define(Token name, Object value) {
    if (values.containsKey(name.lexeme)) values.put(name.lexeme, value);
    else Nyx.error(name.line, "Variable '" + name.lexeme + "' is not declared.");
  }

  public Object get(Token name) {
    if (values.containsKey(name.lexeme)) return values.get(name.lexeme);

    Nyx.error(name.line, "Can not access undeclared variable '" + name.lexeme + "'");
    return null; // FIXME replace with throw
  }

  @Override
  public String toString() {
    return values.entrySet().toString();
  }
}
