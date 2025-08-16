package org.nyx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import org.nyx.buildin.NyxCallable;
import org.nyx.buildin.NyxClass;
import org.nyx.buildin.NyxFunction;
import org.nyx.buildin.NyxGlobals;
import org.nyx.buildin.NyxInstance;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

  public static class RuntimeError extends RuntimeException {
    private final Token token;

    public RuntimeError(Token token, String msg) {
      super(msg);
      this.token = token;
    }
  }

  private final Map<Expr, Integer> locals = new HashMap<>();

  // Global environment
  private Environment environment = new Environment(null);
  private Optional<Object> returnValue = null;

  public Interpreter() {
    for (var pair : NyxGlobals.GLOBALS.entrySet()) {
      environment.declare(pair.getKey(), pair.getValue());
    }
  }

  public void interpret(List<Stmt> statements) {
    try {
      for (Stmt statement : statements) {
        execute(statement);
      }
    } catch (RuntimeError e) {
      Nyx.error(e.token, e.getMessage());
    }
  }

  public void interpret(Expr expression) {
    try {
      Object value = evaluate(expression);
      System.out.println(value);
    } catch (RuntimeError error) {
      Nyx.error(error.token, error.getMessage());
    }
  }

  public void execute(Stmt stmt) {
    stmt.accept(this);
  }

  public void execute(Stmt.Block stmts, Environment env) {
    Environment previous = this.environment;
    try {
      this.environment = env;

      for (Stmt statement : stmts.statements()) {
        execute(statement);
        if (returnValue != null) return;
      }
    } finally {
      this.environment = previous;
    }
  }

  public Object evaluate(Expr expr) {
    return expr.accept(this);
  }

  public void resolve(Expr expr, int depth) {
    locals.put(expr, depth);
  }

  @Override
  public Void visitLetStmt(Stmt.Let stmt) {
    Object value = null;
    if (stmt.initializer() != null) {
      value = evaluate(stmt.initializer());
    }

    environment.declare(stmt.name(), value);
    return null;
  }

  @Override
  public Void visitExpressionStmt(Stmt.Expression stmt) {
    evaluate(stmt.expr());
    return null;
  }

  @Override
  public Void visitBlockStmt(Stmt.Block stmt) {
    execute(stmt, new Environment(environment));
    return null;
  }

  @Override
  public Void visitClassStmt(Stmt.Class stmt) {
    environment.declare(stmt.name(), null);

    NyxClass superclass = null;
    if (stmt.superclass() != null) {
      Object obj = evaluate(stmt.superclass());
      if (obj instanceof NyxClass cast) {
        superclass = cast;
        environment = new Environment(environment);
        environment.declare("super", superclass);
      } else throw new RuntimeError(stmt.superclass().name(), "Superclass must be a class.");
    }

    Map<String, NyxFunction> methods = new HashMap<>();
    for (var method : stmt.methods()) {
      methods.put(method.name().lexeme(), new NyxFunction(method, environment));
    }

    NyxClass created = new NyxClass(stmt.name().lexeme(), superclass, methods);
    environment.define(stmt.name(), created);

    if (superclass != null) {
      environment = environment.getEnclosing();
    }

    return null;
  }

  @Override
  public Void visitFunctionStmt(Stmt.Function stmt) {
    NyxFunction function = new NyxFunction(stmt, environment);
    environment.declare(stmt.name(), function);
    return null;
  }

  @Override
  public Void visitIfStmt(Stmt.If stmt) {
    if (evaluate(stmt.condition()) instanceof Boolean c && c) {
      execute(stmt.ifBranch());
    } else if (stmt.elseBranch() != null) {
      execute(stmt.elseBranch());
    }
    return null;
  }

  @Override
  public Void visitReturnStmt(Stmt.Return stmt) {
    returnValue =
        (stmt.value() != null) ? Optional.ofNullable(evaluate(stmt.value())) : Optional.empty();

    return null;
  }

  @Override
  public Void visitWhileStmt(Stmt.While stmt) {
    while (evaluate(stmt.condition()) instanceof Boolean c && c) {
      execute(stmt.body());
    }
    return null;
  }

  @Override
  public Object visitLogicalExpr(Expr.Logical expr) {
    Object left = evaluate(expr.left());
    checkBoolOperand(expr.operator(), left);

    if (expr.operator().type() == TokenType.OR) {
      if ((boolean) left) return left;
    } else {
      if (!(boolean) left) return left;
    }

    return evaluate(expr.right());
  }

  @Override
  public Object visitSetExpr(Expr.Set expr) {
    Object object = evaluate(expr.object());

    if (object instanceof NyxInstance instance) {
      Object value = evaluate(expr.value());
      switch (expr.operator().type()) {
        case SET -> {
          instance.set(expr.name(), value);
        }
        case SET_ADD -> {
          instance.compute(
              expr.name(),
              (key, present) -> {
                checkNumberOperand(expr.operator(), value);
                checkNumberOperand(expr.operator(), present);
                return (Double) present + (Double) value;
              });
        }
        case SET_SUB -> {
          instance.compute(
              expr.name(),
              (key, present) -> {
                checkNumberOperand(expr.operator(), value);
                checkNumberOperand(expr.operator(), present);
                return (Double) present - (Double) value;
              });
        }
        case SET_MUL -> {
          instance.compute(
              expr.name(),
              (key, present) -> {
                checkNumberOperand(expr.operator(), value);
                checkNumberOperand(expr.operator(), present);
                return (Double) present * (Double) value;
              });
        }
        case SET_DIV -> {
          instance.compute(
              expr.name(),
              (key, present) -> {
                checkNumberOperand(expr.operator(), value);
                checkNumberOperand(expr.operator(), present);
                return (Double) present / (Double) value;
              });
        }
        // Unreachable.
        default -> throw new RuntimeError(expr.operator(), "Unexpected token.");
      }

      return value;
    }

    throw new RuntimeError(expr.name(), "Only instances have fields.");
  }

  @Override
  public Object visitSuperExpr(Expr.Super expr) {
    int distance = locals.get(expr);
    NyxClass superclass = (NyxClass) environment.getAt(distance, "super");
    NyxInstance object = (NyxInstance) environment.getAt(distance - 1, "this");
    NyxFunction method = superclass.findMethod(expr.method());
    if (method == null) {
      throw new RuntimeError(expr.method(), "Undefined property '" + expr.method().lexeme() + "'.");
    }

    return method.bind(object);
  }

  @Override
  public Object visitThisExpr(Expr.This expr) {
    return lookUpVariable(expr.keyword(), expr);
  }

  @Override
  public Object visitVariableExpr(Expr.Variable expr) {
    return lookUpVariable(expr.name(), expr);
  }

  @Override
  public Object visitLiteralExpr(Expr.Literal expr) {
    return expr.value();
  }

  @Override
  public Object visitUnaryExpr(Expr.Unary expr) {
    Object right = evaluate(expr.right());

    return switch (expr.operator().type()) {
      case SUB -> {
        checkNumberOperand(expr.operator(), right);
        yield -(double) right;
      }
      case NOT -> {
        checkBoolOperand(expr.operator(), right);
        yield !(boolean) right;
      }
      // Unreachable.
      default -> throw new RuntimeError(expr.operator(), "Unexpected token.");
    };
  }

  @Override
  public Object visitBinaryExpr(Expr.Binary expr) {
    Object left = evaluate(expr.left());

    return switch (expr.operator().type()) {
      case GREATER -> {
        checkNumberOperand(expr.operator(), left);
        Object right = evaluate(expr.right());
        checkNumberOperand(expr.operator(), right);
        yield (double) left > (double) right;
      }
      case GREATER_EQUAL -> {
        checkNumberOperand(expr.operator(), left);
        Object right = evaluate(expr.right());
        checkNumberOperand(expr.operator(), right);
        yield (double) left >= (double) right;
      }
      case LESS -> {
        checkNumberOperand(expr.operator(), left);
        Object right = evaluate(expr.right());
        checkNumberOperand(expr.operator(), right);
        yield (double) left < (double) right;
      }
      case LESS_EQUAL -> {
        checkNumberOperand(expr.operator(), left);
        Object right = evaluate(expr.right());
        checkNumberOperand(expr.operator(), right);
        yield (double) left <= (double) right;
      }
      case NOT_EQUAL -> !isEqual(left, evaluate(expr.right()));
      case EQUAL -> isEqual(left, evaluate(expr.right()));
      case ADD -> {
        Object right = evaluate(expr.right());
        if (left instanceof Double l && right instanceof Double r) {
          yield l + r;
        }
        if (left instanceof String l) {
          yield l + (right != null ? right.toString() : "nil");
        }

        throw new RuntimeError(expr.operator(), "Expected numbers or strings.");
      }
      case SUB -> {
        checkNumberOperand(expr.operator(), left);
        Object right = evaluate(expr.right());
        checkNumberOperand(expr.operator(), right);
        yield (double) left - (double) right;
      }
      case MUL -> {
        checkNumberOperand(expr.operator(), left);
        Object right = evaluate(expr.right());
        checkNumberOperand(expr.operator(), right);
        yield (double) left * (double) right;
      }
      case DIV -> {
        checkNumberOperand(expr.operator(), left);
        Object right = evaluate(expr.right());
        checkNumberOperand(expr.operator(), right);
        yield (double) left / (double) right;
      }
      // Unreachable.
      default -> throw new RuntimeError(expr.operator(), "Unexpected token.");
    };
  }

  @Override
  public Object visitAssignExpr(Expr.Assign expr) {
    Object value = evaluate(expr.value());

    Integer distance = locals.get(expr);
    switch (expr.operator().type()) {
      case SET -> {
        if (distance != null) {
          environment.defineAt(distance, expr.name(), value);
        } else {
          environment.define(expr.name(), value);
        }
      }
      case SET_ADD -> {
        checkNumberOperand(expr.operator(), value);
        BiFunction<String, Object, Object> func =
            (key, present) -> {
              if (present instanceof Double l) return l + (Double) value;
              else throw new RuntimeError(expr.operator(), "Left side is not a number.");
            };
        if (distance != null) {
          environment.computeAt(distance, expr.name(), func);
        } else {
          environment.compute(expr.name(), func);
        }
      }
      case SET_SUB -> {
        checkNumberOperand(expr.operator(), value);
        BiFunction<String, Object, Object> func =
            (key, present) -> {
              if (present instanceof Double l) return l - (Double) value;
              else throw new RuntimeError(expr.operator(), "Left side is not a number.");
            };
        if (distance != null) {
          environment.computeAt(distance, expr.name(), func);
        } else {
          environment.compute(expr.name(), func);
        }
      }
      case SET_MUL -> {
        checkNumberOperand(expr.operator(), value);
        BiFunction<String, Object, Object> func =
            (key, present) -> {
              if (present instanceof Double l) return l * (Double) value;
              else throw new RuntimeError(expr.operator(), "Left side is not a number.");
            };
        if (distance != null) {
          environment.computeAt(distance, expr.name(), func);
        } else {
          environment.compute(expr.name(), func);
        }
      }
      case SET_DIV -> {
        checkNumberOperand(expr.operator(), value);
        BiFunction<String, Object, Object> func =
            (key, present) -> {
              if (present instanceof Double l) return l / (Double) value;
              else throw new RuntimeError(expr.operator(), "Left side is not a number.");
            };
        if (distance != null) {
          environment.computeAt(distance, expr.name(), func);
        } else {
          environment.compute(expr.name(), func);
        }
      }
      // Unreachable.
      default -> throw new RuntimeError(expr.operator(), "Unexpected token.");
    }

    return value;
  }

  @Override
  public Object visitCallExpr(Expr.Call expr) {
    Object callee = evaluate(expr.callee());
    if (callee instanceof NyxCallable fun) {
      if (fun.aritiy() != expr.arguments().size()) {
        throw new RuntimeError(
            expr.paren(),
            "Expected " + fun.aritiy() + " arguments, but got " + expr.arguments().size() + ".");
      }

      List<Object> arguments = new ArrayList<>();
      for (Expr argument : expr.arguments()) {
        arguments.add(evaluate(argument));
      }

      return fun.call(this, arguments);
    }

    throw new RuntimeError(expr.paren(), "Can only call functions and classes.");
  }

  @Override
  public Object visitGetExpr(Expr.Get expr) {
    Object object = evaluate(expr.object());
    if (object instanceof NyxInstance instance) {
      return instance.get(expr.name());
    }

    throw new RuntimeError(expr.name(), "Only instances have properties.");
  }

  @Override
  public Object visitGroupingExpr(Expr.Grouping expr) {
    return evaluate(expr.expression());
  }

  private Object lookUpVariable(Token name, Expr expr) {
    Integer distance = locals.get(expr);
    if (distance != null) {
      return environment.getAt(distance, name.lexeme());
    } else {
      return environment.get(name);
    }
  }

  private void checkNumberOperand(Token operator, Object operand) {
    if (!(operand instanceof Double)) {
      throw new RuntimeError(operator, "Operand must be a number");
    }
  }

  private void checkBoolOperand(Token operator, Object operand) {
    if (!(operand instanceof Boolean)) {
      throw new RuntimeError(operator, "Operand must be a boolean");
    }
  }

  private boolean isEqual(Object a, Object b) {
    if (a == null) return b == null;

    return a.equals(b);
  }

  public Object getReturnValue() {
    // Function did not return (void function), use default value nil instead.
    if (returnValue == null) return null;

    var temp = returnValue;
    returnValue = null;
    // Get raw value.
    return temp.orElse(null);
  }

  public Environment getEnvironment() {
    return environment;
  }
}
