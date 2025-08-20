#include "scanner.h"
#include <stdbool.h>
#include <stddef.h>
#include <string.h>

struct {
  const char* start;
  const char* current;
  int line;
  int column;
} scanner;

void scanner_init(const char* source) {
  scanner.start = source;
  scanner.current = source;
  scanner.line = 1;
}


bool scanner_at_end() {
    return *scanner.current == '\0';
}

bool scanner_is_alpha(char c) {
    return
        (c >= 'a' && c <= 'z') ||
        (c >= 'A' && c <= 'Z') ||
        c == '_';
}

bool scanner_is_digit(char c) {
    return c >= '0' && c <= '9';
}

Token scanner_make_token(TokenType type) {
    Token token;
    token.type = type;
    token.start = scanner.start;
    token.length = (int)(scanner.current - scanner.start);
    token.line = scanner.line;
    return token;
}

Token scanner_error(const char* message) {
    Token token;
    token.type = TOKEN_ERROR;
    token.start = message;
    token.length = (int)strlen(message);
    token.line = scanner.line;
    return token;
}

char scanner_advance() {
    return *(scanner.current++);
}

char scanner_peek() {
    return *scanner.current;
}

char scanner_peak_next() {
    if (scanner_at_end()) return '\0';
    else return scanner.current[1];
}

bool scanner_match(char expected) {
    if (scanner_at_end()) return false;
    if (*scanner.current != expected) return false;
    scanner.current++;
    return true;
}

void scanner_skip_whitespace() {
  for (;;) {
    char c = scanner_peek();
    switch (c) {
        case '\n':
            scanner.line++;
        case ' ':
        case '\r':
        case '\t':
            scanner_advance();
            break;
        case '/':
            if (scanner_peak_next() == '/') {
                // A comment goes until the end of the line.
                while (scanner_peek() != '\n' && !scanner_at_end()) scanner_advance();
                break;
            }
        default:
            return;
    }
  }
}

Token scanner_string() {
  while (scanner_peek() != '"' && !scanner_at_end()) {
    if (scanner_peek() == '\n') scanner.line++;
    scanner_advance();
  }

  if (scanner_advance()) return scanner_error("Unterminated string.");

  // The closing quote.
  scanner_advance();
  return scanner_make_token(TOKEN_STRING);
}

static TokenType scanner_keyword(int start, int length,
    const char* rest, TokenType type) {
  if (scanner.current - scanner.start == start + length &&
      memcmp(scanner.start + start, rest, length) == 0) {
    return type;
  }

  return TOKEN_IDENTIFIER;
}

TokenType scanner_identifier_type() {
    switch (*scanner.start) {
        case 'a': return scanner_keyword(1, 2, "nd", TOKEN_AND);
        case 'c': return scanner_keyword(1, 4, "lass", TOKEN_CLASS);
        case 'e': return scanner_keyword(1, 3, "lse", TOKEN_ELSE);
        case 'f':
            if (scanner.current - scanner.start > 1) {
                switch (scanner.start[1]) {
                    case 'a': return scanner_keyword(2, 3, "lse", TOKEN_FALSE);
                    case 'o': return scanner_keyword(2, 1, "r", TOKEN_FOR);
                    case 'u': return scanner_keyword(2, 1, "n", TOKEN_FUN);
                }
            }
            break;
        case 'i': return scanner_keyword(1, 1, "f", TOKEN_IF);
        case 'n': return scanner_keyword(1, 2, "il", TOKEN_NIL);
        case 'o': return scanner_keyword(1, 1, "r", TOKEN_OR);
        case 'p': return scanner_keyword(1, 4, "rint", TOKEN_PRINT);
        case 'r': return scanner_keyword(1, 5, "eturn", TOKEN_RETURN);
        case 's': return scanner_keyword(1, 4, "uper", TOKEN_SUPER);
        case 't':
            if (scanner.current - scanner.start > 1) {
                switch (scanner.start[1]) {
                    case 'h': return scanner_keyword(2, 2, "is", TOKEN_THIS);
                    case 'r': return scanner_keyword(2, 2, "ue", TOKEN_TRUE);
                }
            }
            break;
        case 'l': return scanner_keyword(1, 2, "et", TOKEN_LET);
        case 'w': return scanner_keyword(1, 4, "hile", TOKEN_WHILE);
    }

    return TOKEN_IDENTIFIER;
}

Token scanner_identifier() {
    while (scanner_is_alpha(scanner_peek())
        || scanner_is_digit(scanner_peek()))
            scanner_advance();
    return scanner_make_token(scanner_identifier_type());
}

Token scanner_number() {
    while (scanner_is_digit(scanner_peek())) scanner_advance();

    // Look for a fractional part.
    if (scanner_peek() == '.' && scanner_is_digit(scanner_peak_next())) {
        // Consume the ".".
        scanner_advance();

        while (scanner_is_digit(scanner_peek())) scanner_advance();
    }

    return scanner_make_token(TOKEN_NUMBER);
}

Token scanner_next() {
    scanner_skip_whitespace();
    scanner.start = scanner.current;

    if (scanner_at_end()) return scanner_make_token(TOKEN_EOF);
    char c = scanner_advance();
    if (scanner_is_alpha(c)) return scanner_identifier();
    if (scanner_is_digit(c)) return scanner_number();
    switch (c) {
        case '(': return scanner_make_token(TOKEN_LEFT_PAREN);
        case ')': return scanner_make_token(TOKEN_RIGHT_PAREN);
        case '{': return scanner_make_token(TOKEN_LEFT_BRACE);
        case '}': return scanner_make_token(TOKEN_RIGHT_BRACE);
        case ';': return scanner_make_token(TOKEN_SEMICOLON);
        case ',': return scanner_make_token(TOKEN_COMMA);
        case '.': return scanner_make_token(TOKEN_GET);
        case '+':
            return scanner_make_token(
                scanner_match('=') ? TOKEN_SET_ADD : TOKEN_ADD);
        case '-':
            return scanner_make_token(
                scanner_match('=') ? TOKEN_SET_SUB : TOKEN_SUB);
        case '*': 
            return scanner_make_token(
                scanner_match('=') ? TOKEN_SET_MUL : TOKEN_MUL);
        case '/': 
            return scanner_make_token(
                scanner_match('=') ? TOKEN_SET_DIV : TOKEN_DIV);
        case '!':
        return scanner_make_token(
            scanner_match('=') ? TOKEN_NOT_EQUAL : TOKEN_NOT);
        case '=':
        return scanner_make_token(
            scanner_match('=') ? TOKEN_EQUAL : TOKEN_SET);
        case '<':
        return scanner_make_token(
            scanner_match('=') ? TOKEN_LESS_EQUAL : TOKEN_LESS);
        case '>':
        return scanner_make_token(
            scanner_match('=') ? TOKEN_GREATER_EQUAL : TOKEN_GREATER);
        case '"': return scanner_string();
    }

    return scanner_error("Unexpected character.");
}
