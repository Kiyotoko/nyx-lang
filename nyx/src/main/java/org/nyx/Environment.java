package org.nyx;

import java.util.HashMap;
import java.util.Map;

public class Environment {
  private final Environment enclosing;
  private final Map<String, Object> values = new HashMap<>();

  public Environment(Environment enclosing) {
    this.enclosing = enclosing;
  }

  public void declare(Token name, Object value) {
    if (!values.containsKey(name.lexeme)) values.put(name.lexeme, value);
    else Nyx.error(name.line, "Variable '" + name.lexeme + "' is already declared.");
  }

  public void define(Token name, Object value) {
    if (values.containsKey(name.lexeme)) values.put(name.lexeme, value);
    else if (enclosing != null) enclosing.define(name, value);
    else Nyx.error(name.line, "Variable '" + name.lexeme + "' is not declared.");
  }

  public Object get(Token name) {
    if (values.containsKey(name.lexeme)) return values.get(name.lexeme);
    else if (enclosing != null) return enclosing.get(name);

    Nyx.error(name.line, "Can not access undeclared variable '" + name.lexeme + "'");
    return null; // FIXME replace with throw
  }

  @Override
  public String toString() {
    return values.entrySet().toString();
  }
}
