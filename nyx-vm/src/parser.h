#ifndef PARSER_H
#define PARSER_H

#include <stdbool.h>
#include "scanner.h"

typedef enum {
    PREC_NONE,
    PREC_ASSIGNMENT,  // =
    PREC_OR,          // or
    PREC_AND,         // and
    PREC_EQUALITY,    // == !=
    PREC_COMPARISON,  // < > <= >=
    PREC_TERM,        // + -
    PREC_FACTOR,      // * /
    PREC_UNARY,       // ! -
    PREC_CALL,        // . ()
    PREC_PRIMARY
} Precedence;

extern Token* parser_previous();

extern bool parser_had_error();

extern void parser_advance();

extern void parser_consume(TokenType type, const char* message);

extern void parser_precedence(Precedence Precedence);

extern void parser_expression();

extern void parser_grouping();

extern void parser_unary();

extern void parser_binary();

extern void parser_number();

#endif
