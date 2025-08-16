package org.nyx.buildin;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import org.nyx.Interpreter.RuntimeError;
import org.nyx.Token;

public class NyxInstance {
  private final Map<String, Object> fields = new HashMap<>();
  private final NyxClass creator;

  public NyxInstance(NyxClass creator) {
    this.creator = creator;
  }

  public Object get(Token name) {
    if (fields.containsKey(name.lexeme())) {
      return fields.get(name.lexeme());
    }

    NyxFunction method = creator.findMethod(name);
    if (method != null) return method.bind(this);

    throw new RuntimeError(name, "Undefined property '" + name.lexeme() + "'.");
  }

  public void set(Token name, Object value) {
    fields.put(name.lexeme(), value);
  }

  public void compute(Token name, BiFunction<String, Object, Object> func) {
    fields.compute(name.lexeme(), func);
  }

  @Override
  public String toString() {
    return "<" + creator.getName() + "#" + this.hashCode() + ">";
  }
}
