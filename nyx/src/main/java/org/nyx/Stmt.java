package org.nyx;

import java.util.List;

public interface Stmt {
  interface Visitor<R> {
    R visitBlockStmt(Block stmt);

    R visitClassStmt(Class stmt);

    R visitFunctionStmt(Function stmt);

    R visitIfStmt(If stmt);

    R visitLetStmt(Let stmt);

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

  record Function(Token name, List<Token> params, Stmt.Block body) implements Stmt {
    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitFunctionStmt(this);
    }
  }

  record If(Expr condition, Block ifBranch, Block elseBranch) implements Stmt {
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

  record While(Expr condition, Block body) implements Stmt {
    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitWhileStmt(this);
    }
  }

  <R> R accept(Visitor<R> visitor);
}
