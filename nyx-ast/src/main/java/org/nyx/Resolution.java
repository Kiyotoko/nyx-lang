package org.nyx;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nyx.buildin.NyxModule;

public class Resolution implements Expr.Visitor<Void>, Stmt.Visitor<Void> {

  private final Interpreter interpreter;

  // Deque that contains all scopes. A scope is a map that contains all variables
  // mapped to false if it was not initialised and true if it was initialised.
  private final Deque<Map<String, Boolean>> scopes = new ArrayDeque<>();

  public Resolution(Interpreter interpreter) {
    this.interpreter = interpreter;
  }

  public void resolve(List<Stmt> statements) {
    for (Stmt statement : statements) {
      resolve(statement);
    }
  }

  public void resolve(Stmt stmt) {
    stmt.accept(this);
  }

  public void resolve(Expr expr) {
    expr.accept(this);
  }

  private void resolveLocal(Expr expr, Token name) {
    int i = 0;
    for (var scope : scopes) {
      if (scope.containsKey(name.lexeme())) {
        interpreter.resolve(expr, i);
        return;
      } else i++;
    }
  }

  private void resolveFunction(Stmt.Function function) {
    beginScope();
    for (Token param : function.params()) {
      declare(param);
      define(param);
    }
    resolve(function.body().statements());
    endScope();
  }

  @Override
  public Void visitBinaryExpr(Expr.Binary expr) {
    resolve(expr.left());
    resolve(expr.right());
    return null;
  }

  @Override
  public Void visitCallExpr(Expr.Call expr) {
    resolve(expr.callee());

    for (Expr argument : expr.arguments()) {
      resolve(argument);
    }

    return null;
  }

  @Override
  public Void visitGetExpr(Expr.Get expr) {
    resolve(expr.object());
    return null;
  }

  @Override
  public Void visitGroupingExpr(Expr.Grouping expr) {
    resolve(expr.expression());
    return null;
  }

  @Override
  public Void visitLiteralExpr(Expr.Literal expr) {
    return null;
  }

  @Override
  public Void visitLogicalExpr(Expr.Logical expr) {
    resolve(expr.left());
    resolve(expr.right());
    return null;
  }

  @Override
  public Void visitAssignExpr(Expr.Assign expr) {
    resolve(expr.value());
    resolveLocal(expr, expr.name());
    return null;
  }

  @Override
  public Void visitSetExpr(Expr.Set expr) {
    resolve(expr.value());
    resolve(expr.object());
    return null;
  }

  @Override
  public Void visitSuperExpr(Expr.Super expr) {
    resolveLocal(expr, expr.keyword());
    return null;
  }

  @Override
  public Void visitThisExpr(Expr.This expr) {
    resolveLocal(expr, expr.keyword());
    return null;
  }

  @Override
  public Void visitUnaryExpr(Expr.Unary expr) {
    resolve(expr.right());
    return null;
  }

  @Override
  public Void visitVariableExpr(Expr.Variable expr) {
    if (!scopes.isEmpty()) {
      Boolean v = scopes.peek().get(expr.name().lexeme());
      if (v != null && !v) {
        Nyx.error(expr.name(), "Can't read local variable in its own initializer.");
      }
    }

    resolveLocal(expr, expr.name());
    return null;
  }

  @Override
  public Void visitBlockStmt(Stmt.Block stmt) {
    beginScope();
    resolve(stmt.statements());
    endScope();
    return null;
  }

  @Override
  public Void visitClassStmt(Stmt.Class stmt) {
    declare(stmt.name());
    define(stmt.name());

    if (stmt.superclass() != null) {
      if (stmt.superclass().name() == stmt.name()) {
        Nyx.error(stmt.superclass().name(), "A class can't inherit from itself.");
      }
      resolve(stmt.superclass());

      beginScope();
      scopes.peek().put("super", true);
    }

    beginScope();
    scopes.peek().put("this", true);
    for (var method : stmt.methods()) {
      resolveFunction(method);
    }
    endScope();
    if (stmt.superclass() != null) endScope();

    return null;
  }

  @Override
  public Void visitExpressionStmt(Stmt.Expression stmt) {
    resolve(stmt.expr());
    return null;
  }

  @Override
  public Void visitFunctionStmt(Stmt.Function stmt) {
    declare(stmt.name());
    define(stmt.name());

    resolveFunction(stmt);
    return null;
  }

  @Override
  public Void visitIfStmt(Stmt.If stmt) {
    resolve(stmt.condition());
    resolve(stmt.ifBranch());
    if (stmt.elseBranch() != null) resolve(stmt.elseBranch());
    return null;
  }

  @Override
  public Void visitImportStmt(Stmt.Import stmt) {
    // Preload module here
    NyxModule.from(stmt.paths());
    return null;
  }

  @Override
  public Void visitLetStmt(Stmt.Let stmt) {
    declare(stmt.name());
    if (stmt.initializer() != null) {
      resolve(stmt.initializer());
    }
    define(stmt.name());
    return null;
  }

  @Override
  public Void visitReturnStmt(Stmt.Return stmt) {
    if (stmt.value() != null) {
      resolve(stmt.value());
    }

    return null;
  }

  @Override
  public Void visitWhileStmt(Stmt.While stmt) {
    resolve(stmt.condition());
    resolve(stmt.body());
    return null;
  }

  private void declare(Token name) {
    if (scopes.isEmpty()) {
      return;
    }

    scopes.peek().put(name.lexeme(), false);
  }

  private void define(Token name) {
    if (scopes.isEmpty()) {
      return;
    }

    scopes.peek().put(name.lexeme(), true);
  }

  private void beginScope() {
    scopes.push(new HashMap<>());
  }

  private void endScope() {
    scopes.pop();
  }
}
