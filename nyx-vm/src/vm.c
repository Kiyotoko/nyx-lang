#include "vm.h"
#include "chunk.h"
#include "debug.h"
#include <stdio.h>
#include <stdlib.h>

#define DEBUG_TRACE 0

static VM vm = { .chunk=NULL, .ip=0 };

void vm_init() {
    vm.stack_top = vm.stack;
}

#define READ_BYTE() (*vm.ip++)
#define READ_VALUE() (value_list_get(vm.chunk->constants, READ_BYTE()))
#define READ_BIN_OP(op) \
    do { \
        Value b = vm_stack_pop(); \
        Value a = vm_stack_pop(); \
        vm_stack_push((a) op (b)); \
    } while (0)

int vm_run() {
    for (;;) {
        #if DEBUG_TRACE
            printf("          ");
            for (Value* slot = vm.stack; slot < vm.stack_top; slot++) {
                printf("[ ");
                dbg_value(*slot);
                printf(" ]");
            }
            printf("\n");
            dbg_instruction(vm.chunk, vm.ip - vm.chunk->codes);
        #endif
        uint8_t instruction = READ_BYTE();
        switch (instruction) {
            case OP_CONSTANT: {
                Value constant =  READ_VALUE();
                vm_stack_push(constant);
                break;
            }
            case OP_NEGATE:
                vm_stack_push(-vm_stack_pop());
                break;
            case OP_ADD:
                READ_BIN_OP(+);
                break;
            case OP_SUB:
                READ_BIN_OP(-);
                break;
            case OP_MUL:
                READ_BIN_OP(*);
                break;
            case OP_DIV:
                READ_BIN_OP(/);
                break;
            case OP_RETURN:
                dbg_value(vm_stack_pop());
                return EXIT_SUCCESS;
        }
    }
}

int vm_interpret(Chunk* chunk) {
    vm.chunk = chunk;
    vm.ip = chunk->codes;

    return vm_run();
}

void vm_stack_push(Value value) {
    *(vm.stack_top++) = value;
}

Value vm_stack_pop() {
    return *(--vm.stack_top);
}

void vm_free() {

}
