package org.nyx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {
  private static class ParseError extends RuntimeException {}

  private final List<Token> tokens;
  private int current = 0;

  public Parser(List<Token> tokens) {
    this.tokens = new ArrayList(tokens);
  }

  public List<Stmt> parse() {
    List<Stmt> statements = new ArrayList<>();
    while (!isAtEnd()) {
      statements.add(declaration());
    }

    return statements;
  }

  // Try to parse a declaration, else parses a statement.
  private Stmt declaration() {
    try {
      if (match(TokenType.LET)) return varDeclaration();
      if (match(TokenType.FUN)) return funDeclaration();
      if (match(TokenType.CLASS)) return classDeclaration();

      return statement();
    } catch (ParseError error) {
      synchronize();
      return null;
    }
  }

  private Stmt varDeclaration() {
    Token name = consume(TokenType.IDENTIFIER, "Expect variable name.");

    Expr initializer = null;
    if (match(TokenType.SET)) {
      initializer = expression();
    }

    match(TokenType.SEMICOLON);
    return new Stmt.Let(name, initializer);
  }

  private Stmt.Function funDeclaration() {
    Token name = consume(TokenType.IDENTIFIER, "Expect function name.");
    consume(TokenType.LEFT_PAREN, "Expect '(' after function name.");
    List<Token> parameters = new ArrayList<>();
    if (!check(TokenType.RIGHT_PAREN)) {
      do {
        if (parameters.size() >= 127) {
          throw error(peek(), "Can't have more than 127 parameters.");
        }

        parameters.add(consume(TokenType.IDENTIFIER, "Expect parameter name."));
      } while (match(TokenType.COMMA));
    }
    consume(TokenType.RIGHT_PAREN, "Expect ')' after parameters.");

    consume(TokenType.LEFT_BRACE, "Expect '{' before function body.");
    return new Stmt.Function(name, parameters, blockStatement());
  }

  private Stmt classDeclaration() {
    Token name = consume(TokenType.IDENTIFIER, "Expect class name.");
    Expr.Variable superclass = null;
    if (match(TokenType.LEFT_PAREN)) {
      consume(TokenType.IDENTIFIER, "Expect superclass name.");
      superclass = new Expr.Variable(previous());
      consume(TokenType.RIGHT_PAREN, "Expect ')' after superclass.");
    }

    consume(TokenType.LEFT_BRACE, "Expect '{' before class body.");

    List<Stmt.Function> methods = new ArrayList<>();
    while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
      methods.add(funDeclaration());
    }

    consume(TokenType.RIGHT_BRACE, "Expect '}' after class body.");

    return new Stmt.Class(name, superclass, methods);
  }

  // Try to parses a statement, else parses a expression.
  private Stmt statement() {
    if (match(TokenType.LEFT_BRACE)) return blockStatement();
    if (match(TokenType.IF)) return ifStatement();
    if (match(TokenType.IMPORT)) return importStatement();
    if (match(TokenType.WHILE)) return whileStatement();
    if (match(TokenType.FOR)) return forStatement();
    if (match(TokenType.RETURN)) return returnStatement();

    return expressionStatement();
  }

  private Stmt.Block blockStatement() {
    List<Stmt> statements = new ArrayList<>();

    while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
      statements.add(declaration());
    }

    consume(TokenType.RIGHT_BRACE, "Expect '}' after block.");
    return new Stmt.Block(statements);
  }

  private Stmt.If ifStatement() {
    consume(TokenType.LEFT_PAREN, "Expect '(' after 'if'.");
    Expr condition = expression();
    consume(TokenType.RIGHT_PAREN, "Expect ')' after if condition.");

    Stmt thenBranch = statement();
    Stmt elseBranch = null;
    if (match(TokenType.ELSE)) {
      elseBranch = statement();
    }

    return new Stmt.If(condition, thenBranch, elseBranch);
  }

  private Stmt.Import importStatement() {
    List<Token> paths = new ArrayList<>();
    do {
      paths.add(advance());
    } while (match(TokenType.GET));
    match(TokenType.SEMICOLON);

    return new Stmt.Import(paths);
  }

  private Stmt whileStatement() {
    consume(TokenType.LEFT_PAREN, "Expect '(' after 'while'.");
    Expr condition = expression();
    consume(TokenType.RIGHT_PAREN, "Expect ')' after condition.");
    Stmt body = statement();

    return new Stmt.While(condition, body);
  }

  private Stmt forStatement() {
    consume(TokenType.LEFT_PAREN, "Expect '(' after 'for'.");

    Stmt initializer;
    if (match(TokenType.SEMICOLON)) {
      initializer = null;
    } else if (match(TokenType.LET)) {
      initializer = varDeclaration();
    } else {
      initializer = expressionStatement();
    }

    Expr condition;
    if (!check(TokenType.SEMICOLON)) {
      condition = expression();
    } else {
      condition = new Expr.Literal(true);
    }
    consume(TokenType.SEMICOLON, "Expect ';' after loop condition.");

    Expr increment = null;
    if (!check(TokenType.RIGHT_PAREN)) {
      increment = expression();
    }
    consume(TokenType.RIGHT_PAREN, "Expect ')' after for clauses.");

    Stmt body = statement();
    if (increment != null) {
      body = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(increment)));
    }
    body = new Stmt.While(condition, body);
    if (initializer != null) {
      body = new Stmt.Block(Arrays.asList(initializer, body));
    }

    return body;
  }

  private Stmt returnStatement() {
    Token keyword = previous();
    Expr value = null;
    if (!check(TokenType.SEMICOLON)) {
      value = expression();
    }

    match(TokenType.SEMICOLON);
    return new Stmt.Return(keyword, value);
  }

  private Stmt.Expression expressionStatement() {
    Expr expr = expression();
    match(TokenType.SEMICOLON);
    return new Stmt.Expression(expr);
  }

  private Expr expression() {
    return assignment();
  }

  private Expr assignment() {
    Expr expr = or();

    if (match(
        TokenType.SET,
        TokenType.SET_ADD,
        TokenType.SET_SUB,
        TokenType.SET_MUL,
        TokenType.SET_DIV)) {
      Token equals = previous();
      Expr value = assignment();

      if (expr instanceof Expr.Variable variable)
        return new Expr.Assign(variable.name(), equals, value);
      if (expr instanceof Expr.Get get)
        return new Expr.Set(get.object(), get.name(), equals, value);

      throw error(equals, "Invalid assignment target.");
    }

    return expr;
  }

  private Expr or() {
    Expr expr = and();

    while (match(TokenType.OR)) {
      Token operator = previous();
      Expr right = and();
      expr = new Expr.Logical(expr, operator, right);
    }

    return expr;
  }

  private Expr and() {
    Expr expr = equality();

    while (match(TokenType.AND)) {
      Token operator = previous();
      Expr right = equality();
      expr = new Expr.Logical(expr, operator, right);
    }

    return expr;
  }

  private Expr equality() {
    Expr expr = comparison();

    while (match(TokenType.EQUAL, TokenType.NOT_EQUAL)) {
      Token operator = previous();
      Expr right = comparison();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr comparison() {
    Expr expr = term();

    while (match(
        TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
      Token operator = previous();
      Expr right = term();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr term() {
    Expr expr = factor();

    while (match(TokenType.ADD, TokenType.SUB)) {
      Token operator = previous();
      Expr right = factor();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr factor() {
    Expr expr = unary();

    while (match(TokenType.MUL, TokenType.DIV)) {
      Token operator = previous();
      Expr right = unary();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr unary() {
    if (match(TokenType.NOT, TokenType.SUB)) {
      Token operator = previous();
      Expr right = unary();
      return new Expr.Unary(operator, right);
    }

    return call();
  }

  private Expr call() {
    Expr expr = primary();

    for (; ; ) {
      if (match(TokenType.LEFT_PAREN)) {
        expr = finishCall(expr);
      } else if (match(TokenType.GET)) {
        Token name = consume(TokenType.IDENTIFIER, "Expect property name after '.'.");
        expr = new Expr.Get(expr, name);
      } else break;
    }

    return expr;
  }

  private Expr.Call finishCall(Expr callee) {
    List<Expr> arguments = new ArrayList<>();
    if (!check(TokenType.RIGHT_PAREN)) {
      do {
        if (arguments.size() >= 127) {
          throw error(peek(), "Can't have more than 127 arguments.");
        }
        arguments.add(expression());
      } while (match(TokenType.COMMA));
    }

    Token paren = consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments.");

    return new Expr.Call(callee, paren, arguments);
  }

  private Expr primary() {
    return switch (advance().type()) {
      case FALSE -> Expr.Literal.FALSE;
      case TRUE -> Expr.Literal.TRUE;
      case NIL -> Expr.Literal.NIL;
      case NUMBER, STRING -> new Expr.Literal(previous().literal());
      case THIS -> new Expr.This(previous());
      case SUPER -> {
        Token keyword = previous();
        consume(TokenType.GET, "Expect '.' after 'super'.");
        Token method = consume(TokenType.IDENTIFIER, "Expect superclass method name.");
        yield new Expr.Super(keyword, method);
      }
      case IDENTIFIER -> new Expr.Variable(previous());
      case LEFT_PAREN -> {
        Expr expr = expression();
        consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
        yield new Expr.Grouping(expr);
      }
      default -> {
        throw error(previous(), "Expect expression.");
      }
    };
  }

  private void synchronize() {
    advance();

    while (!isAtEnd()) {
      if (previous().type() == TokenType.SEMICOLON) return;

      switch (peek().type()) {
        case CLASS, FUN, LET, FOR, IF, WHILE, RETURN -> {
          return;
        }
        default -> advance();
      }
    }
  }

  private Token consume(TokenType type, String message) {
    if (check(type)) return advance();

    throw error(peek(), message);
  }

  private Parser.ParseError error(Token token, String message) {
    Nyx.error(token, message);
    return new ParseError();
  }

  private boolean match(TokenType... types) {
    for (TokenType type : types) {
      if (check(type)) {
        advance();
        return true;
      }
    }

    return false;
  }

  private boolean check(TokenType type) {
    if (isAtEnd()) return false;
    return peek().type() == type;
  }

  private Token advance() {
    if (!isAtEnd()) current++;
    return previous();
  }

  private boolean isAtEnd() {
    return peek().type() == TokenType.EOF;
  }

  private Token peek() {
    return tokens.get(current);
  }

  private Token previous() {
    return tokens.get(current - 1);
  }
}
