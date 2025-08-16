package org.nyx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nyx.buildin.NyxCallable;
import org.nyx.buildin.NyxFunction;
import org.nyx.buildin.NyxGlobals;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

  private static class RuntimeError extends RuntimeException {
    private final Token token;

    public RuntimeError(Token token, String msg) {
      super(msg);
      this.token = token;
    }
  }

  private final Map<Expr, Integer> locals = new HashMap<>();

  // Global environment
  private Environment environment = new Environment(null);
  private Object returnValue = null;

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
    throw new UnsupportedOperationException("Not supported yet.");
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
    returnValue = evaluate(stmt.value());
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
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Object visitSuperExpr(Expr.Super expr) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Object visitThisExpr(Expr.This expr) {
    throw new UnsupportedOperationException("Not supported yet.");
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
        if (left instanceof Double && right instanceof Double) {
          yield (double) left + (double) right;
        }
        if (left instanceof String && right instanceof String) {
          yield (String) left + (String) right;
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
    if (distance != null) {
      environment.defineAt(distance, expr.name(), value);
    } else {
      environment.define(expr.name(), value);
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
    throw new UnsupportedOperationException("Not supported yet.");
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
    var temp = returnValue;
    returnValue = null;
    return temp;
  }

  public Environment getEnvironment() {
    return environment;
  }
}
