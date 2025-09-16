package org.nyx.buildin;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.nyx.Interpreter;

public final class NyxGlobals {
  private NyxGlobals() {
    throw new UnsupportedOperationException();
  }

  public static Map<String, Object> GLOBALS =
      Map.of(
          "time",
              new NyxCallable() {
                @Override
                public Object call(Interpreter interpreter, List<Object> args) {
                  return (double) System.currentTimeMillis();
                }

                @Override
                public int aritiy() {
                  return 0;
                }

                @Override
                public String toString() {
                  return "<native fn>";
                }
              },
          "print",
              new NyxCallable() {
                @Override
                public Object call(Interpreter interpreter, List<Object> args) {
                  Object arg = args.get(0);
                  String str = (arg != null ? arg.toString() : "nil");
                  System.out.println(str);
                  return str;
                }

                @Override
                public int aritiy() {
                  return 1;
                }

                @Override
                public String toString() {
                  return "<native fn>";
                }
              },
          "input",
              new NyxCallable() {
                private static final Scanner scanner = new Scanner(System.in);

                @Override
                public Object call(Interpreter interpreter, List<Object> args) {
                  return scanner.nextLine();
                }

                @Override
                public int aritiy() {
                  return 0;
                }

                @Override
                public String toString() {
                  return "<native fn>";
                }
              });
}
