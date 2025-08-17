#ifndef DEBUG_H
#define DEBUG_H

#include "chunk.h"

void dbg_chunk(Chunk* chunk, const char* name);
int dbg_instruction(Chunk* chunk, int offset);

#endif