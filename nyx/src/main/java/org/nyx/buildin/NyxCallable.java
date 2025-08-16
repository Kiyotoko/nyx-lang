package org.nyx.buildin;

import java.util.List;
import org.nyx.Interpreter;

public interface NyxCallable {
  Object call(Interpreter interpreter, List<Object> args);

  int aritiy();
}
