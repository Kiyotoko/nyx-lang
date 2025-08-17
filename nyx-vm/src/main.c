#include "chunk.h"
#include "debug.h"

int main() {
    Chunk* chunk = chunk_create();
    chunk_write(chunk, OP_RETURN);
    dbg_chunk(chunk, "test chunk");
    chunk_free(chunk);

    return 0;
}