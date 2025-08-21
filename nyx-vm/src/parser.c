#include <stdlib.h>
#include "parser.h"
#include "debug.h"
#include "compiler.h"
#include "scanner.h"

struct {
    Token current;
    Token previous;
    bool had_error;
} parser;

typedef void (*ParseFn)();

typedef struct {
    ParseFn prefix;
    ParseFn infix;
    Precedence precedence;
} ParseRule;

Token* parser_previous() {
    return &parser.previous;
}

bool parser_had_error() {
    return parser.had_error;
}

void parser_advance() {
    parser.previous = parser.current;

    for (;;) {
        parser.current = scanner_next();
        if (parser.current.type != TOKEN_ERROR) break;

        error_at(&parser.previous, parser.current.start);
    }
}

void parser_consume(TokenType type, const char* message) {
  if (parser.current.type == type) {
    parser_advance();
    return;
  }

  error_at(&parser.previous, message);
}

ParseRule rules[] = {
  [TOKEN_LEFT_PAREN]    = {parser_grouping, NULL,   PREC_NONE},
  [TOKEN_RIGHT_PAREN]   = {NULL,     NULL,   PREC_NONE},
  [TOKEN_LEFT_BRACE]    = {NULL,     NULL,   PREC_NONE}, 
  [TOKEN_RIGHT_BRACE]   = {NULL,     NULL,   PREC_NONE},
  [TOKEN_COMMA]         = {NULL,     NULL,   PREC_NONE},
  [TOKEN_GET]           = {NULL,     NULL,   PREC_NONE},
  [TOKEN_SUB]           = {parser_unary,    parser_binary, PREC_TERM},
  [TOKEN_ADD]           = {NULL,     parser_binary, PREC_TERM},
  [TOKEN_SEMICOLON]     = {NULL,     NULL,   PREC_NONE},
  [TOKEN_DIV]           = {NULL,     parser_binary, PREC_FACTOR},
  [TOKEN_MUL]           = {NULL,    parser_binary, PREC_FACTOR},
  [TOKEN_NOT]           = {NULL,     NULL,   PREC_NONE},
  [TOKEN_NOT_EQUAL]     = {NULL,     NULL,   PREC_NONE},
  [TOKEN_SET]           = {NULL,     NULL,   PREC_NONE},
  [TOKEN_EQUAL]         = {NULL,     NULL,   PREC_NONE},
  [TOKEN_GREATER]       = {NULL,     NULL,   PREC_NONE},
  [TOKEN_GREATER_EQUAL] = {NULL,     NULL,   PREC_NONE},
  [TOKEN_LESS]          = {NULL,     NULL,   PREC_NONE},
  [TOKEN_LESS_EQUAL]    = {NULL,     NULL,   PREC_NONE},
  [TOKEN_IDENTIFIER]    = {NULL,     NULL,   PREC_NONE},
  [TOKEN_STRING]        = {NULL,     NULL,   PREC_NONE},
  [TOKEN_NUMBER]        = {parser_number,   NULL,   PREC_NONE},
  [TOKEN_AND]           = {NULL,     NULL,   PREC_NONE},
  [TOKEN_CLASS]         = {NULL,     NULL,   PREC_NONE},
  [TOKEN_ELSE]          = {NULL,     NULL,   PREC_NONE},
  [TOKEN_FALSE]         = {NULL,     NULL,   PREC_NONE},
  [TOKEN_FOR]           = {NULL,     NULL,   PREC_NONE},
  [TOKEN_FUN]           = {NULL,     NULL,   PREC_NONE},
  [TOKEN_IF]            = {NULL,     NULL,   PREC_NONE},
  [TOKEN_NIL]           = {NULL,     NULL,   PREC_NONE},
  [TOKEN_OR]            = {NULL,     NULL,   PREC_NONE},
  [TOKEN_PRINT]         = {NULL,     NULL,   PREC_NONE},
  [TOKEN_RETURN]        = {NULL,     NULL,   PREC_NONE},
  [TOKEN_SUPER]         = {NULL,     NULL,   PREC_NONE},
  [TOKEN_THIS]          = {NULL,     NULL,   PREC_NONE},
  [TOKEN_TRUE]          = {NULL,     NULL,   PREC_NONE},
  [TOKEN_LET]           = {NULL,     NULL,   PREC_NONE},
  [TOKEN_WHILE]         = {NULL,     NULL,   PREC_NONE},
  [TOKEN_ERROR]         = {NULL,     NULL,   PREC_NONE},
  [TOKEN_EOF]           = {NULL,     NULL,   PREC_NONE},
};

ParseRule* parser_get_rule(TokenType type) {
    return rules + type;
}

void parser_precedence(Precedence precedence) {
  parser_advance();
  ParseFn prefix_rule = parser_get_rule(parser.previous.type)->prefix;
  if (prefix_rule == NULL) {
    error_at(parser_previous(), "Expect expression.");
    return;
  }

  prefix_rule();

  while (precedence <= parser_get_rule(parser.current.type)->precedence) {
    parser_advance();
    ParseFn infix_rule = parser_get_rule(parser.previous.type)->infix;
    infix_rule();
  }
}

void parser_expression() {
    parser_precedence(PREC_ASSIGNMENT);
}

void parser_grouping() {
  parser_expression();
  parser_consume(TOKEN_RIGHT_PAREN, "Expect ')' after expression.");
}

void parser_unary() {
  TokenType type = parser.previous.type;

  // Compile the operand.
  parser_precedence(PREC_UNARY);

  // Emit the operator instruction.
  switch (type) {
    case TOKEN_SUB:
        compiler_emit_byte(OP_NEGATE);
        break;
    default:
        error_at(&parser.previous, "Unexpected token.");
        return; // Unreachable.
  }
}

void parser_binary() {
  TokenType type = parser.previous.type;
  ParseRule* rule = parser_get_rule(type);
  parser_precedence((Precedence) (rule->precedence + 1));

  switch (type) {
    case TOKEN_ADD:
        compiler_emit_byte(OP_ADD); break;
    case TOKEN_SUB:
        compiler_emit_byte(OP_SUB); break;
    case TOKEN_MUL:
        compiler_emit_byte(OP_MUL); break;
    case TOKEN_DIV:
        compiler_emit_byte(OP_DIV); break;
    default: return; // Unreachable.
  }
}

void parser_number() {
    double value = strtod(parser.previous.start, NULL);
    compiler_emit_constant(value);
}
