package org.nyx.buildin;

import java.util.List;
import java.util.Map;
import org.nyx.Interpreter;
import org.nyx.Token;

public final class NyxClass implements NyxCallable {
  private final Map<String, NyxFunction> methods;
  private final NyxFunction initializer;
  private final NyxClass superclass;
  private final String name;

  public NyxClass(String name, NyxClass superclass, Map<String, NyxFunction> methods) {
    this.methods = methods;
    this.initializer = methods.remove("init");
    this.superclass = superclass;
    this.name = name;
  }

  public NyxFunction findMethod(Token name) {
    NyxFunction func = methods.get(name.lexeme());
    if (func == null && superclass != null) {
      func = superclass.findMethod(name);
    }
    if (func != null) return func;

    throw new Interpreter.RuntimeError(name, "Could not find method '" + name.lexeme() + "'.");
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> args) {
    NyxInstance instance = new NyxInstance(this);
    if (initializer != null) {
      if (initializer.bind(instance).call(interpreter, args) != null) {
        throw new Interpreter.RuntimeError(
            initializer.declaration().name(), "Did not expect non 'nil' return inside init.");
      }
    }
    return instance;
  }

  @Override
  public int aritiy() {
    return initializer != null ? initializer.aritiy() : 0;
  }

  @Override
  public String toString() {
    return "<class " + name + ">";
  }

  public String getName() {
    return name;
  }
}
