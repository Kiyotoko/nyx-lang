#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include "chunk.h"
#include "value.h"

#define DEFAULT_CAPACITY 4

Chunk* chunk_create() {
    Chunk* created = malloc(sizeof(Chunk));
    if (!created) {
        exit(EXIT_FAILURE);
        return NULL;
    }

    created->codes = calloc(DEFAULT_CAPACITY, sizeof(uint8_t));
    created->capacity = DEFAULT_CAPACITY;
    created->len = 0;
    created->constants = value_list_create();
    return created;
}

void chunk_write(Chunk* chunk, uint8_t byte) {
    if (chunk->capacity <= chunk->len) {
        chunk->capacity *= 2;
        chunk->codes = realloc(chunk->codes, chunk->capacity * sizeof(uint8_t));
    }
    chunk->codes[chunk->len++] = byte;
}

size_t chunk_add_constant(Chunk* chunk, Value value) {
    return value_list_append(chunk->constants, value);
}

void chunk_free(Chunk* chunk) {
    value_list_free(chunk->constants);
    free(chunk->codes);
    free(chunk);
}
