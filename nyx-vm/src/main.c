#include "vm.h"
#include <stdio.h>
#include <stdlib.h>

void run_promt() {
    char line[1024];
    for (;;) {
        printf("> ");

        if (!fgets(line, sizeof(line), stdin)) {
            printf("\n");
            break;
        }

        vm_interpret(line);
    }
}

char* readFile(const char* path) {
    FILE* file = fopen(path, "rb");
    if (!file) {
        fprintf(stderr, "Could not open file \"%s\".\n", path);
        exit(74);
    }

    fseek(file, 0L, SEEK_END);
    size_t fileSize = ftell(file);
    rewind(file);

    char* buffer = malloc(fileSize + 1);
    if (!buffer) {
        fprintf(stderr, "Not enough memory to read \"%s\".\n", path);
        exit(74);
    }
    size_t bytesRead = fread(buffer, sizeof(char), fileSize, file);
    buffer[bytesRead] = '\0';
    if (bytesRead < fileSize) {
        fprintf(stderr, "Could not read file \"%s\".\n", path);
        exit(74);
    }

    fclose(file);
    return buffer;
}

void run_file(char* path) {
  char* source = readFile(path);
  int result = vm_interpret(source);
  free(source); 

  if (result) exit(result);
}

int main(int argc, char** argv) {
    vm_init();
    if (argc == 1) {
        run_promt();
    } else if (argc == 2) {
        run_file(argv[1]);
    } else {
        fprintf(stderr, "Usage: clox [path]\n");
        exit(64);
    }
    vm_free();

    return 0;
}