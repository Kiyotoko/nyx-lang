#include <stdint.h>
#include <stdlib.h>
#include "chunk.h"

#define DEFAULT_CAPACITY 4

Chunk* chunk_create() {
    Chunk* created = malloc(sizeof(Chunk));
    created->code = malloc(sizeof(uint8_t) * DEFAULT_CAPACITY);
    created->capacity = DEFAULT_CAPACITY;
    created->len = 0;
    return created;
}

void chunk_write(Chunk* chunk, uint8_t byte) {
    if (chunk->capacity < chunk->len + 1) {
        size_t old_capacity = chunk->capacity;
        chunk->capacity = old_capacity * 2;
        chunk->code = realloc(chunk->code, sizeof(uint8_t));
    }
    chunk->code[chunk->len++] = byte;
}

void chunk_free(Chunk* chunk) {
    free(chunk->code);
    free(chunk);
}

