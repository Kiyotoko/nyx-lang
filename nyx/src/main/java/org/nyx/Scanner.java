package org.nyx;

import java.util.ArrayList;
import java.util.List;

public class Scanner {
  private final String source;
  private final List<Token> tokens = new ArrayList<>();
  private int start = 0;
  private int current = 0;
  private int line = 1;

  public Scanner(String source) {
    this.source = source;
  }

  public List<Token> scanTokens() {
    while (!isAtEnd()) {
      // We are at the beginning of the next lexeme.
      start = current;
      scanToken();
    }

    tokens.add(new Token(TokenType.EOF, "", null, line));
    return tokens;
  }

  private void scanToken() {
    char c = advance();
    switch (c) {
      case '(' -> addToken(TokenType.LEFT_PAREN);
      case ')' -> addToken(TokenType.RIGHT_PAREN);
      case '{' -> addToken(TokenType.LEFT_BRACE);
      case '}' -> addToken(TokenType.RIGHT_BRACE);
      case ',' -> addToken(TokenType.COMMA);
      case '.' -> addToken(TokenType.GET);
      case '+' -> addToken(TokenType.ADD);
      case '-' -> addToken(TokenType.SUB);
      case '*' -> addToken(TokenType.MUL);
      case ';' -> addToken(TokenType.SEMICOLON);
      case '!' -> addToken(match('=') ? TokenType.NOT_EQUAL : TokenType.NOT);
      case '=' -> addToken(match('=') ? TokenType.EQUAL : TokenType.SET);
      case '<' -> addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
      case '>' -> addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
      case '/' -> {
        if (match('/')) {
          // A comment goes until the end of the line.
          while (peek() != '\n' && !isAtEnd()) advance();
        } else {
          addToken(TokenType.DIV);
        }
      }
      // Ignore whitespace.
      case ' ', '\r', '\t' -> {}
      case '\n' -> line++;
      case '"' -> string();
      default -> {
        if (isDigit(c)) {
          number();
        } else if (isAlpha(c)) {
          identifier();
        } else {
          Nyx.error(line, "Unexpected character.");
        }
      }
    }
  }

  private void identifier() {
    while (isAlphaNumeric(peek())) advance();

    String text = source.substring(start, current);
    TokenType type =
        switch (text) {
          case "and" -> TokenType.AND;
          case "class" -> TokenType.CLASS;
          case "else" -> TokenType.ELSE;
          case "false" -> TokenType.FALSE;
          case "for" -> TokenType.FOR;
          case "fun" -> TokenType.FUN;
          case "if" -> TokenType.IF;
          case "nil" -> TokenType.NIL;
          case "or" -> TokenType.OR;
          case "return" -> TokenType.RETURN;
          case "super" -> TokenType.SUPER;
          case "this" -> TokenType.THIS;
          case "true" -> TokenType.TRUE;
          case "let" -> TokenType.LET;
          case "while" -> TokenType.WHILE;
          default -> TokenType.IDENTIFIER;
        };
    addToken(type);
  }

  private void number() {
    while (isDigit(peek())) advance();

    // Look for a fractional part.
    if (peek() == '.' && isDigit(peekNext())) {
      // Consume the "."
      advance();

      while (isDigit(peek())) advance();
    }

    addToken(TokenType.NUMBER, Double.valueOf(source.substring(start, current)));
  }

  private void string() {
    while (peek() != '"' && !isAtEnd()) {
      if (peek() == '\n') line++;
      advance();
    }

    if (isAtEnd()) {
      Nyx.error(line, "Unterminated string.");
      return;
    }

    // The closing ".
    advance();

    // Trim the surrounding quotes.
    String value = source.substring(start + 1, current - 1);
    addToken(TokenType.STRING, value);
  }

  private boolean match(char expected) {
    if (isAtEnd()) return false;
    if (source.charAt(current) != expected) return false;

    current++;
    return true;
  }

  private char peek() {
    if (isAtEnd()) return '\0';
    return source.charAt(current);
  }

  private char peekNext() {
    if (current + 1 >= source.length()) return '\0';
    return source.charAt(current + 1);
  }

  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  private boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
  }

  private boolean isAlphaNumeric(char c) {
    return isAlpha(c) || isDigit(c);
  }

  private boolean isAtEnd() {
    return current >= source.length();
  }

  private char advance() {
    return source.charAt(current++);
  }

  private void addToken(TokenType type) {
    addToken(type, null);
  }

  private void addToken(TokenType type, Object literal) {
    String text = source.substring(start, current);
    tokens.add(new Token(type, text, literal, line));
  }
}
