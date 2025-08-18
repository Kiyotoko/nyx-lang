#include "chunk.h"
#include "debug.h"
#include "vm.h"

int main() {
    vm_init();
    Chunk* chunk = chunk_create();

    size_t a = chunk_add_constant(chunk, 1.2);
    chunk_write(chunk, OP_CONSTANT);
    chunk_write(chunk, a);
    chunk_write(chunk, OP_NEGATE);
    size_t b = chunk_add_constant(chunk, 3.7);
    chunk_write(chunk, OP_CONSTANT);
    chunk_write(chunk, b);
    chunk_write(chunk, OP_ADD);
    chunk_write(chunk, OP_RETURN);

    dbg_chunk(chunk, "test chunk");
    vm_interpret(chunk);

    chunk_free(chunk);
    vm_free();

    return 0;
}