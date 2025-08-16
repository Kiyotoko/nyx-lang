package org.nyx;

import java.util.List;

public interface Expr {
  static interface Visitor<R> {
    R visitBinaryExpr(Binary expr);

    R visitCallExpr(Call expr);

    R visitGetExpr(Get expr);

    R visitGroupingExpr(Grouping expr);

    R visitLiteralExpr(Literal expr);

    R visitLogicalExpr(Logical expr);

    R visitAssignExpr(Assign expr);

    R visitSetExpr(Set expr);

    R visitSuperExpr(Super expr);

    R visitThisExpr(This expr);

    R visitUnaryExpr(Unary expr);

    R visitVariableExpr(Variable expr);
  }

  record Literal(Object value) implements Expr {
    public static final Literal FALSE = new Literal(false);
    public static final Literal TRUE = new Literal(true);
    public static final Literal NIL = new Literal(null);

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitLiteralExpr(this);
    }
  }

  record Unary(Token operator, Expr right) implements Expr {
    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitUnaryExpr(this);
    }
  }

  record Binary(Expr left, Token operator, Expr right) implements Expr {
    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitBinaryExpr(this);
    }
  }

  record Assign(Token name, Token operator, Expr value) implements Expr {
    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitAssignExpr(this);
    }
  }

  record Call(Expr callee, Token paren, List<Expr> arguments) implements Expr {
    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitCallExpr(this);
    }
  }

  record Set(Expr object, Token name, Token operator, Expr value) implements Expr {
    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitSetExpr(this);
    }
  }

  record Get(Expr object, Token name) implements Expr {
    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitGetExpr(this);
    }
  }

  record Grouping(Expr expression) implements Expr {
    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitGroupingExpr(this);
    }
  }

  record Logical(Expr left, Token operator, Expr right) implements Expr {
    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitLogicalExpr(this);
    }
  }

  record Super(Token keyword, Token method) implements Expr {
    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitSuperExpr(this);
    }
  }

  record This(Token keyword) implements Expr {
    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitThisExpr(this);
    }
  }

  record Variable(Token name) implements Expr {
    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitVariableExpr(this);
    }
  }

  <R> R accept(Visitor<R> visitor);
}
