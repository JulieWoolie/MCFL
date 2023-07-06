package me.jules.mcfl;

import static me.jules.mcfl.TokenStream.EOF;
import static me.jules.mcfl.interpreter.Bindings.FLAG_CONST;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import me.jules.mcfl.ast.FunctionFile;
import me.jules.mcfl.interpreter.Utils;
import me.jules.mcfl.interpreter.ReturnValue;
import me.jules.mcfl.interpreter.Scope;

public class FunctionLang {

  public static final ScriptCallable PRINT = (ctx, scope, params) -> {
    String s = Utils.toString(params);
    System.out.println(s);
    return ReturnValue.NO_RETURN;
  };

  public static final ScriptCallable FLOOR = (ctx, scope, params) -> {
    Utils.ensureParamCount(params, 1);
    double n = params[0].getDouble();
    return ReturnValue.directWrap(Math.floor(n));
  };

  public static final ScriptCallable CEIL = (ctx, scope, params) -> {
    Utils.ensureParamCount(params, 1);
    double n = params[0].getDouble();
    return ReturnValue.directWrap(Math.ceil(n));
  };

  public static Scope defineStandardScope() {
    Scope scope = new Scope();

    scope.defineValue("print", PRINT, FLAG_CONST);
    scope.defineValue("floor", FLOOR, FLAG_CONST);
    scope.defineValue("ceil",   CEIL, FLAG_CONST);

    scope.setProperty("systemTimeMillis", new SystemTimeProperty());
    scope.setProperty("scriptTime",       new ScriptTimeProperty());

    return scope;
  }

  public static FunctionFile parse(CharSequence seq) {
    if (seq instanceof StringBuffer buf) {
      return parse(buf);
    } else {
      return parse(new StringBuffer(seq));
    }
  }

  public static FunctionFile parse(URL url) throws IOException {
    return parse(url, Charset.defaultCharset());
  }

  public static FunctionFile parse(URL url, Charset set) throws IOException {
    var stream = url.openStream();
    var reader = new InputStreamReader(stream, set);

    FunctionFile f = parse(reader);

    reader.close();
    stream.close();

    return f;
  }

  public static FunctionFile parse(Path path) throws IOException {
    return parse(path, Charset.defaultCharset());
  }

  public static FunctionFile parse(Path path, Charset set) throws IOException {
    var reader = Files.newBufferedReader(path, set);
    FunctionFile f = parse(reader);
    reader.close();
    return f;
  }

  public static FunctionFile parse(Reader reader) throws IOException {
    StringBuffer buf = new StringBuffer();
    int ch;

    while ((ch = reader.read()) != EOF) {
      buf.appendCodePoint(ch);
    }

    return parse(buf);
  }

  public static FunctionFile parse(StringBuffer buf) {
    TokenStream stream = new TokenStream(buf);
    Parser parser = new Parser(stream);
    return parser.parse();
  }
}
