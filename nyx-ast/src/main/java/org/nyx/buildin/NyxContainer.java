package org.nyx.buildin;

import java.util.function.BiFunction;

import org.nyx.Token;

public interface NyxContainer {
  public Object get(Token name);

  public void set(Token name, Object value);
  
  public void compute(Token name, BiFunction<String, Object, Object> func);
}