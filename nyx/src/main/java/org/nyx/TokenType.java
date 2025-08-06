package org.nyx;

public enum TokenType {
  // Single-character tokens.
  LEFT_PAREN,
  RIGHT_PAREN,
  LEFT_BRACE,
  RIGHT_BRACE,
  COMMA,
  GET,
  ADD,
  SUB,
  MUL,
  DIV,
  SEMICOLON,

  // One or two character tokens.
  NOT,
  NOT_EQUAL,
  SET,
  EQUAL,
  GREATER,
  GREATER_EQUAL,
  LESS,
  LESS_EQUAL,

  // Literals.
  IDENTIFIER,
  STRING,
  NUMBER,

  // Keywords.
  AND,
  CLASS,
  ELSE,
  FALSE,
  FUN,
  FOR,
  IF,
  NIL,
  OR,
  RETURN,
  SUPER,
  THIS,
  TRUE,
  LET,
  WHILE,

  EOF
}
