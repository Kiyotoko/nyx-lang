package org.nyx;

import java.util.List;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

  private static class RuntimeError extends RuntimeException {
    private final Token token;

    public RuntimeError(Token token, String msg) {
      super(msg);
      this.token = token;
    }
  }

  private final Environment environment = new Environment();

  public void interpret(List<Stmt> statements) {
    try {
      for (Stmt statement : statements) {
        execute(statement);
      }
    } catch (RuntimeError e) {
      Nyx.error(e.token.line, e.getMessage());
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

  public Object evaluate(Expr expr) {
    return expr.accept(this);
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
  public Void visitWhileStmt(Stmt.While stmt) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Object visitLogicalExpr(Expr.Logical expr) {
    throw new UnsupportedOperationException("Not supported yet.");
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
    return environment.get(expr.name());
  }

  @Override
  public Void visitBlockStmt(Stmt.Block stmt) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Void visitClassStmt(Stmt.Class stmt) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Void visitFunctionStmt(Stmt.Function stmt) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Void visitIfStmt(Stmt.If stmt) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Object visitLiteralExpr(Expr.Literal expr) {
    return expr.value();
  }

  @Override
  public Object visitUnaryExpr(Expr.Unary expr) {
    Object right = evaluate(expr.right());

    return switch (expr.operator().type) {
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

    return switch (expr.operator().type) {
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
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Object visitCallExpr(Expr.Call expr) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Object visitGetExpr(Expr.Get expr) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Object visitGroupingExpr(Expr.Grouping expr) {
    return evaluate(expr.expression());
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
    if (a == null && b == null) return true;
    if (a == null) return false;

    return a.equals(b);
  }

  public Environment getEnvironment() {
    return environment;
  }
}
