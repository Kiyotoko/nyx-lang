package org.nyx;

public record Token(TokenType type, String lexeme, Object literal, int line) {

}
