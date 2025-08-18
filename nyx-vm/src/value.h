#ifndef VALUE_H
#define VALUE_H

#include <stddef.h>

typedef double Value;

typedef struct {
    Value* values;
    size_t capacity;
    size_t len;
} ValueList;

extern ValueList* value_list_create();
extern size_t value_list_append(ValueList* list, Value value);
extern Value value_list_get(ValueList* list, size_t index);
extern void value_list_free(ValueList* list);

#endif