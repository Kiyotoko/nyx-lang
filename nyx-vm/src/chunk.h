#ifndef CHUNK_H
#define CHUNK_H

#include <stdint.h>
#include <stddef.h>

typedef enum {
    OP_RETURN
} OpCode;

typedef struct {
    uint8_t* code;
    size_t capacity;
    size_t len;
} Chunk;

extern Chunk* chunk_create();

extern void chunk_write(Chunk* chunk, uint8_t byte);

extern void chunk_free(Chunk* chunk);

#endif