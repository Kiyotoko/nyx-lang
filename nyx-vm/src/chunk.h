#ifndef CHUNK_H
#define CHUNK_H

#include "value.h"
#include <stdint.h>
#include <stddef.h>

typedef enum {
    OP_CONSTANT,
    OP_NEGATE,
    OP_ADD,
    OP_SUB,
    OP_MUL,
    OP_DIV,
    OP_RETURN
} OpCode;

typedef struct {
    uint8_t* codes;
    size_t capacity;
    size_t len;
    ValueList* constants;
} Chunk;

extern Chunk* chunk_create();

extern void chunk_write(Chunk* chunk, uint8_t byte);

extern size_t chunk_add_constant(Chunk* chunk, Value value);

extern void chunk_free(Chunk* chunk);

#endif