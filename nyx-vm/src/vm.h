#ifndef VM_H
#define VM_H

#include "chunk.h"
#include <stdint.h>

#define STACK_SIZE 255

typedef struct {
  Chunk* chunk;
  uint8_t* ip;
  Value stack[STACK_SIZE];
  Value* stack_top;
} VM;

extern void vm_init();

extern int vm_interpret(Chunk* chunk);

extern void vm_stack_push(Value value);

extern Value vm_stack_pop();

extern void vm_free();

#endif