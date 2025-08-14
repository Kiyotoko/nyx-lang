package org.nyx;

import java.util.List;

interface NyxCallable {
    Object call(Interpreter interpreter, List<Object> args);

    int aritiy();
}
