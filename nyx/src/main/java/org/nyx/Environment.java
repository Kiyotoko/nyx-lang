package org.nyx;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class Environment {
  private final Environment enclosing;
  private final Map<String, Object> values = new HashMap<>();

  public Environment(Environment enclosing) {
    this.enclosing = enclosing;
  }

  public void declare(String name, Object value) {
    values.put(name, value);
  }

  public void declare(Token name, Object value) {
    if (!values.containsKey(name.lexeme())) values.put(name.lexeme(), value);
    else Nyx.error(name, "Variable '" + name.lexeme() + "' is already declared.");
  }

  public void define(Token name, Object value) {
    if (values.containsKey(name.lexeme())) values.put(name.lexeme(), value);
    else if (enclosing != null) enclosing.define(name, value);
    else Nyx.error(name, "Variable '" + name.lexeme() + "' is not declared.");
  }

  public void defineAt(int distance, Token name, Object value) {
    ancestor(distance).values.put(name.lexeme(), value);
  }

  public void compute(Token name, BiFunction<String, Object, Object> function) {
    if (values.containsKey(name.lexeme())) values.compute(name.lexeme(), function);
    else if (enclosing != null) enclosing.compute(name, function);
    else Nyx.error(name, "Variable '" + name.lexeme() + "' is not declared.");
  }

  public void computeAt(int distance, Token name, BiFunction<String, Object, Object> function) {
    ancestor(distance).compute(name, function);
  }

  public Object get(Token name) {
    if (values.containsKey(name.lexeme())) return values.get(name.lexeme());
    else if (enclosing != null) return enclosing.get(name);

    Nyx.error(name, "Can not access undeclared variable '" + name.lexeme() + "'");
    return null; // FIXME replace with throw
  }

  public Object getAt(int distance, String name) {
    return ancestor(distance).values.get(name);
  }

  private Environment ancestor(int distance) {
    Environment environment = this;
    for (int i = 0; i < distance; i++) {
      environment = environment.enclosing;
    }

    return environment;
  }

  @Override
  public String toString() {
    return values.entrySet().toString() + " /-> " + enclosing;
  }

  public Environment getEnclosing() {
    return enclosing;
  }
}
