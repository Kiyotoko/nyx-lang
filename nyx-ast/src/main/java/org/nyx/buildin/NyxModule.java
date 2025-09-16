package org.nyx.buildin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  private static final Map<String, NyxModule> LOADED_MODULES = new HashMap<>();

  public static NyxModule from(List<Token> path) {
    // We assert that the path is never empty and therefore always contains at least one token.
    assert !path.isEmpty();
    StringBuilder builder = new StringBuilder(path.get(0).lexeme());
    Token name = path.get(0);
    for (int i = 1; i < path.size(); i++) {
      builder.append(File.separatorChar).append(path.get(i).lexeme());
      name = path.get(i);
    }
    builder.append(".nyx");

    // Look at local files
    File file = new File(builder.toString());
    if (file.exists()) return NyxModule.from(name, file);

    // Look at library files
    builder.insert(0, File.separatorChar).insert(0, ".nyx");
    file = new File(builder.toString());
    if (file.exists()) return NyxModule.from(name, file);

    // Look at stdlib files
    builder.insert(0, File.separatorChar).insert(0, System.getProperty("user.home"));
    file = new File(builder.toString());
    if (file.exists()) return NyxModule.from(name, file);

    throw new RuntimeError(
        name, "Could not find module in local libray or std, looked at " + file.getAbsolutePath());
  }

  public static NyxModule from(Token name, File file) {
    String key = file.getAbsolutePath();
    NyxModule module;
    if (LOADED_MODULES.containsKey(key)) {
      module = LOADED_MODULES.get(key);
      if (module == null)
        throw new RuntimeError(name, "Recursive import detected for file: " + key);
    } else {
      // Use null to mark that we currently load this module
      LOADED_MODULES.put(key, null);
      module = new NyxModule(name, file);
      LOADED_MODULES.put(key, module);
    }
    return module;
  }

  private NyxModule(Token name, File file) {
    try (FileInputStream stream = new FileInputStream(file)) {
      Scanner scanner =
          new Scanner(
              file.getAbsolutePath(), new String(stream.readAllBytes(), Charset.defaultCharset()));
      Parser parser = new Parser(scanner.scanTokens());
      List<Stmt> stmts = parser.parse();
      Interpreter interpreter = new Interpreter();
      new Resolution(interpreter).resolve(stmts);
      interpreter.interpret(stmts);
      this.name = name;
      this.environment = interpreter.getEnvironment();
    } catch (IOException ex) {
      throw new RuntimeError(name, "Could not import module: " + ex.getMessage());
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
