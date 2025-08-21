#ifndef COMPILER_H
#define COMPILER_H

#include <stdbool.h>
#include "chunk.h"

extern void compiler_emit_byte(uint8_t byte);

extern void compiler_emit_bytes(uint8_t byte1, uint8_t byte2);

extern void compiler_emit_return();

extern void compiler_emit_constant(Value value);

extern bool compile(const char* source, Chunk* chunk);

#endif