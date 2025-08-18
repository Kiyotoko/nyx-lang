#include "debug.h"
#include "chunk.h"
#include "value.h"
#include <stdint.h>
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
  uint8_t instruction = chunk->codes[offset++];
  switch (instruction) {
    case OP_CONSTANT: {
      uint8_t constant = chunk->codes[offset++];
      printf("%-16s %4d ", "OP_CONSTANT", constant);
      Value value = value_list_get(chunk->constants, constant);
      dbg_value(value);
      break;
    }
    case OP_NEGATE:
      puts("OP_NEGATE");
      break;
    case OP_ADD:
      puts("OP_ADD");
      break;
    case OP_SUB:
      puts("OP_SUB");
      break;
    case OP_MUL:
      puts("OP_MUL");
      break;
    case OP_DIV:
      puts("OP_DIV");
      break;
    case OP_RETURN:
      puts("OP_RETURN");
      break;
    default:
      printf("Unknown opcode %d\n", instruction);
      break;
  }
  return offset;
}

void dbg_value(Value value) {
  printf("'%g'\n", value);
}
