package org.nyx;

public enum TokenType {
  // Single-character tokens.
  LEFT_PAREN,
  RIGHT_PAREN,
  LEFT_BRACE,
  RIGHT_BRACE,
  COMMA,
  GET,
  SEMICOLON,

  // One or two character tokens.
  NOT,
  NOT_EQUAL,
  SET,
  SET_ADD,
  SET_SUB,
  SET_MUL,
  SET_DIV,
  ADD,
  SUB,
  MUL,
  DIV,
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
