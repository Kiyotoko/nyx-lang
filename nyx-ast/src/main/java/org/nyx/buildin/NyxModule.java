package org.nyx.buildin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BiFunction;

import org.nyx.Environment;
import org.nyx.Interpreter;
import org.nyx.Interpreter.RuntimeError;
import org.nyx.Parser;
import org.nyx.Resolution;
import org.nyx.Scanner;
import org.nyx.Stmt;
import org.nyx.Token;

public class NyxModule implements NyxContainer {
    private final Environment environment;
    private final Token name;

    private static final Map<String, Future<NyxModule>> LOADED_MODULES = new HashMap<>();
    private static final ExecutorService MODULE_LOADER = Executors.newWorkStealingPool();

    public static Future<NyxModule> from(List<Token> paths) {
        StringBuilder builder = new StringBuilder(paths.get(0).lexeme());
        Token moduleName = paths.get(0);
        for (int i = 1; i < paths.size(); i++) {
            builder.append(File.separatorChar).append(paths.get(i).lexeme());
            moduleName = paths.get(i);
        }
        builder.append(".nyx");
        final String path = builder.toString();
        if (LOADED_MODULES.containsKey(path)) {
            return LOADED_MODULES.get(path);
        } else {
            final Token name = moduleName;
            Future<NyxModule> future = MODULE_LOADER.submit(() -> {
                try {
                    File file = new File(path);
                    if (!file.exists()) {
                        file = new File(".nyx" + File.separatorChar + path);
                        if (!file.exists()) {
                            var fileName = System.getProperty("user.home") + File.separatorChar + ".nyx" + File.separatorChar + path;
                            file = new File(fileName);
                            if (!file.exists()) {
                                throw new RuntimeError(name, "Could not find module in local libray or std, looked at " + fileName);
                            }
                        }
                    }
                    return new NyxModule(name, file);
                } catch (IOException ex) {
                    throw new RuntimeError(name, "Could not import module: " + ex.getMessage());
                }
            });
            LOADED_MODULES.put(path, future);
            return future;
        }
    }

    public NyxModule(Token name, File file) throws IOException {
        try (FileInputStream stream = new FileInputStream(file)) {
            Scanner scanner = new Scanner(file.getAbsolutePath(), new String(stream.readAllBytes(), Charset.defaultCharset()));
            Parser parser = new Parser(scanner.scanTokens());
            List<Stmt> stmts = parser.parse();
            Interpreter interpreter = new Interpreter();
            Resolution resolution = new Resolution(interpreter);
            resolution.resolve(stmts);
            interpreter.interpret(stmts);
            this.name = name;
            this.environment = interpreter.getEnvironment();
        }
    }

    @Override
    public Object get(Token name) {
        return environment.get(name);
    }

    @Override
    public void set(Token name, Object value) {
        throw new RuntimeError(name, "Can not set properties in nyx module.");
    }

    @Override
    public void compute(Token name, BiFunction<String, Object, Object> func) {
        throw new RuntimeError(name, "Can not set properties in nyx module.");
    }

    @Override
    public String toString() {
        return "<module " + this.name.lexeme() + ">";
    }

    public Token getName() {
        return name;
    }
}