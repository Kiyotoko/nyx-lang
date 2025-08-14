package org.nyx;

import java.util.List;

public interface Stmt {
  interface Visitor<R> {
    R visitBlockStmt(Block stmt);

    R visitClassStmt(Class stmt);

    R visitExpressionStmt(Expression stmt);

    R visitFunctionStmt(Function stmt);

    R visitIfStmt(If stmt);

    R visitLetStmt(Let stmt);

    R visitReturnStmt(Return stmt);

    R visitWhileStmt(While stmt);
  }

  record Block(List<Stmt> statements) implements Stmt {
    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitBlockStmt(this);
    }
  }

  record Class(Token name, Expr.Variable classname, List<Stmt.Function> methods) implements Stmt {
    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitClassStmt(this);
    }
  }

  record Expression(Expr expr) implements Stmt {
    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitExpressionStmt(this);
    }
  }

  record Function(Token name, List<Token> params, Stmt.Block body) implements Stmt {
    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitFunctionStmt(this);
    }
  }

  record If(Expr condition, Stmt ifBranch, Stmt elseBranch) implements Stmt {
    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitIfStmt(this);
    }
  }

  record Let(Token name, Expr initializer) implements Stmt {
    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitLetStmt(this);
    }
  }

  record Return(Token keyword, Expr value) implements Stmt {
    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitReturnStmt(this);
    }
  }

  record While(Expr condition, Stmt body) implements Stmt {
    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitWhileStmt(this);
    }
  }

  <R> R accept(Visitor<R> visitor);
}
