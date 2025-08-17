package org.nyx.buildin;

import java.util.List;
import org.nyx.Environment;
import org.nyx.Interpreter;
import org.nyx.Stmt;

public record NyxFunction(Stmt.Function declaration, Environment closure) implements NyxCallable {
  public NyxFunction bind(NyxInstance instance) {
    Environment environment = new Environment(closure);
    environment.declare("this", instance);
    return new NyxFunction(declaration, environment);
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    Environment environment = new Environment(closure);
    for (int i = 0; i < declaration.params().size(); i++) {
      environment.declare(declaration.params().get(i), arguments.get(i));
    }

    interpreter.execute(declaration.body(), environment);
    return interpreter.getReturnValue();
  }

  @Override
  public int aritiy() {
    return declaration.params().size();
  }

  @Override
  public String toString() {
    return "<fn " + declaration.name().lexeme() + ">";
  }
}
