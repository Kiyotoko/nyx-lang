#include "debug.h"
#include <stdio.h>

void dbg_chunk(Chunk *chunk, const char *name) {
  printf("== %s ==\n", name);

  int offset = 0;
  while (offset < chunk->len) {
    offset = dbg_instruction(chunk, offset);
  }
}

int dbg_instruction(Chunk *chunk, int offset) {
  printf("%04d ", offset);
  uint8_t instruction = chunk->code[offset];
  switch (instruction) {
    case OP_RETURN:
      printf("OP_RETURN\n");
      return offset + 1;
    default:
      printf("Unknown opcode %d\n", instruction);
      return offset + 1;
  }
}
