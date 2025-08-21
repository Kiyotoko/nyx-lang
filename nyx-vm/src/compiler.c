#include "compiler.h"
#include "chunk.h"
#include "debug.h"
#include "parser.h"
#include "scanner.h"

#define DEBUG_TRACE 0

#if DEBUG_TRACE
#include <stdio.h>
#endif


Chunk* compile_target;

void compiler_emit_byte(uint8_t byte) {
    chunk_write(compile_target, byte);
}

void compiler_emit_bytes(uint8_t byte1, uint8_t byte2) {
    compiler_emit_byte(byte1);
    compiler_emit_byte(byte2);
}

void compiler_emit_return() {
    chunk_write(compile_target, OP_RETURN);
}

uint8_t compiler_add_constant(Value value) {
    int constant = chunk_add_constant(compile_target, value);
    if (constant > UINT8_MAX) {
        error_at(parser_previous(), "Too many constants in one chunk.");
        return 0;
    }

    return (uint8_t) constant;
}

void compiler_emit_constant(Value value) {
    compiler_emit_bytes(OP_CONSTANT, compiler_add_constant(value));
}

bool compile(const char* source, Chunk* chunk) {
    scanner_init(source);
    #if DEBUG_TRACE
    int line = -1;
    for (;;) {
        Token token = scanner_next();
        if (token.line != line) {
            printf("%4d ", token.line);
            line = token.line;
        } else {
            printf("   | ");
        }
        printf("%2d '%.*s'\n", token.type, token.length, token.start); 

        if (token.type == TOKEN_EOF) break;
    }
    #endif
    compile_target = chunk;
    parser_advance();
    parser_expression();
    parser_consume(TOKEN_EOF, "Expect end of expression.");
    compiler_emit_return();
    return !parser_had_error();
}