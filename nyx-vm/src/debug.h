#ifndef DEBUG_H
#define DEBUG_H

#include "chunk.h"

extern void dbg_chunk(Chunk* chunk, const char* name);

extern int dbg_instruction(Chunk* chunk, int offset);

extern void dbg_value(Value value);

#endif