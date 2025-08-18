#include "value.h"
#include <stddef.h>
#include <stdlib.h>

#define DEFAULT_CAPACITY 8

ValueList* value_list_create() {
    ValueList* created = malloc(sizeof(ValueList));
    if (!created) {
        exit(EXIT_FAILURE);
        return NULL;
    }
    created->values = calloc(8, sizeof(Value));
    created->capacity = DEFAULT_CAPACITY;
    created->len = 0;
    return created;
}

size_t value_list_append(ValueList* list, Value value) {
    size_t index = list->len;
    if (list->capacity <= index) {
        list->capacity *= 2;
        list->values = realloc(list->values, list->capacity * sizeof(Value));
    }
    list->values[list->len++] = value;
    return index;
}


Value value_list_get(ValueList* list, size_t index) {
    return list->values[index];
}

void value_list_free(ValueList* list) {
    free(list->values);
    free(list);
}
