package org.nyx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Nyx {
  private final class Ansi {
    private static final String CSI = "\033[";
    private static final String RESET = CSI + "0m";
    private static final String BOLD = CSI + "1m";
    private static final String RED = CSI + "91m";
    private static final String BLUE = CSI + "94m";
  }

  private static boolean hadError = false;

  public static void main(String[] args) throws IOException {
    if (args.length > 1) {
      System.out.println("Usage: vision [script]");
      System.exit(64);
    } else if (args.length == 1) {
      runFile(args[0]);
    } else {
      runPrompt();
    }

    if (hadError) System.exit(65);
  }

  public static void runFile(String path) throws IOException {
    byte[] bytes = Files.readAllBytes(Paths.get(path));
    run(path, new String(bytes, Charset.defaultCharset()), new Interpreter());
  }

  public static void runPrompt() throws IOException {
    InputStreamReader input = new InputStreamReader(System.in);
    BufferedReader reader = new BufferedReader(input);
    Interpreter interpreter = new Interpreter();

    for (; ; ) {
      System.out.print("> ");
      String line = reader.readLine();
      if (line == null) {
        System.out.println();
        break;
      }
      hadError = false;
      run("STDIO", line, interpreter);
    }
  }

  private static void run(String filename, String source, Interpreter interpreter) {
    Scanner scanner = new Scanner(filename, source);
    List<Token> tokens = scanner.scanTokens();
    Parser parser = new Parser(tokens);
    List<Stmt> statements = parser.parse();

    // Stop if there was a syntax error.
    if (hadError) return;

    new Resolution(interpreter).resolve(statements);
    interpreter.interpret(statements);
  }

  public static void error(String filename, int line, int column, String message) {
    report(filename, line, column, message);
  }

  public static void error(Token token, String message) {
    report(token.filename(), token.line(), token.column(), message);
  }

  private static void report(String filename, int line, int column, String message) {
    System.err.println(
        Ansi.RED + Ansi.BOLD + "error: " + Ansi.RESET + Ansi.BOLD + message + Ansi.RESET);
    System.err.println(Ansi.BLUE + " --> " + Ansi.RESET + filename + ":" + line + ":" + column);
    if (!filename.equals("STDIO")) {
      try {
        int i = 1;
        var iter = Files.lines(Path.of(filename)).iterator();
        while (iter.hasNext()) {
          var content = iter.next();
          if (i++ == line) {
            System.err.println(Ansi.BLUE + " | " + Ansi.RESET + content);
            break;
          }
        }
        System.err.println(
            Ansi.BLUE
                + " | "
                + Ansi.RESET
                + " ".repeat(column - 1)
                + Ansi.RED
                + "^ "
                + message
                + Ansi.RESET);
      } catch (IOException ex) {
        System.err.println("Could not open source file.");
      }
    }
    hadError = true;
  }
}
