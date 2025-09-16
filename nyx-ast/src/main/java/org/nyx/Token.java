package org.nyx;

public record Token(
    TokenType type, String filename, String lexeme, Object literal, int line, int column) {}
