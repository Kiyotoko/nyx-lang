#ifndef DEBUG_H
#define DEBUG_H

#include "chunk.h"
#include "scanner.h"

extern void dbg_chunk(Chunk* chunk, const char* name);

extern int dbg_instruction(Chunk* chunk, int offset);

extern void dbg_value(Value value);

extern void error_at(Token* token, const char* message);

extern void error(int line, int column, const char* message);

#endif