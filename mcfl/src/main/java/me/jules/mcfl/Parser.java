package me.jules.mcfl;

import static me.jules.mcfl.TokenType.ASSIGN;
import static me.jules.mcfl.TokenType.BINARY;
import static me.jules.mcfl.TokenType.COMMA;
import static me.jules.mcfl.TokenType.COMMAND_STRING;
import static me.jules.mcfl.TokenType.CONST;
import static me.jules.mcfl.TokenType.CURLY_CLOSE;
import static me.jules.mcfl.TokenType.CURLY_START;
import static me.jules.mcfl.TokenType.DOLLAR_SIGN;
import static me.jules.mcfl.TokenType.ELSE;
import static me.jules.mcfl.TokenType.EOF;
import static me.jules.mcfl.TokenType.FALSE;
import static me.jules.mcfl.TokenType.FUNC;
import static me.jules.mcfl.TokenType.GLOBAL_LET;
import static me.jules.mcfl.TokenType.HEX;
import static me.jules.mcfl.TokenType.ID;
import static me.jules.mcfl.TokenType.IF;
import static me.jules.mcfl.TokenType.LET;
import static me.jules.mcfl.TokenType.NULL;
import static me.jules.mcfl.TokenType.NUMBER;
import static me.jules.mcfl.TokenType.OCTAL;
import static me.jules.mcfl.TokenType.PAREN_CLOSE;
import static me.jules.mcfl.TokenType.PAREN_START;
import static me.jules.mcfl.TokenType.QUOTED_STRING;
import static me.jules.mcfl.TokenType.RETURN;
import static me.jules.mcfl.TokenType.SEMICOLON;
import static me.jules.mcfl.TokenType.TRUE;

import java.util.StringJoiner;
import me.jules.mcfl.ast.Block;
import me.jules.mcfl.ast.BooleanLiteral;
import me.jules.mcfl.ast.CallExpr;
import me.jules.mcfl.ast.CommandStatement;
import me.jules.mcfl.ast.ExprStatement;
import me.jules.mcfl.ast.Expression;
import me.jules.mcfl.ast.FunctionFile;
import me.jules.mcfl.ast.FunctionStatement;
import me.jules.mcfl.ast.Identifier;
import me.jules.mcfl.ast.IfStatement;
import me.jules.mcfl.ast.NullLiteral;
import me.jules.mcfl.ast.NumberLiteral;
import me.jules.mcfl.ast.ReturnStatement;
import me.jules.mcfl.ast.Statement;
import me.jules.mcfl.ast.StringLiteral;
import me.jules.mcfl.ast.VariableDefinition;
import me.jules.mcfl.ast.VariableDefinition.Type;

public class Parser {

  private final TokenStream stream;

  private final Errors errors;

  public Parser(TokenStream stream) {
    this.stream = stream;
    this.errors = stream.errors();
  }

  public Token next() {
    return stream.nextToken();
  }

  public Token peek() {
    return stream.peekToken();
  }

  public boolean matches(TokenType type) {
    return peek().is(type);
  }

  public boolean matches(TokenType... types) {
    return peek().is(types);
  }

  public Token expect(TokenType type) {
    Token next = next();

    if (!next.is(type)) {
      expectedTokenError(next, type);
    }

    return next;
  }

  public Token expect(TokenType... types) {
    Token next = next();

    if (!next.is(types)) {
      expectedTokenError(next, types);
    }

    return next;
  }

  private void expectedTokenError(Token t, TokenType... types) {
    expectedTokenError("Expected ${expected}, but found ${actual}", t, types);
  }

  private void expectedTokenError(String format, Token t, TokenType... types) {
    String typesMessage;

    if (types.length == 1) {
      typesMessage = types[0].toString();
    } else {
      StringJoiner joiner = new StringJoiner(", ", "one of: ", "");
      for (var type: types) {
        joiner.add(type.toString());
      }
      typesMessage = joiner.toString();
    }

    String message = format
        .replace("${expected}", typesMessage)
        .replace("${actual}", t.type().toString());

    error(t.location(), message);
  }

  private void error(String format, Object... args) {
    error(null, format, args);
  }

  private void error(Location loc, String format, Object... args) {
    String message = format.formatted(args);
    errors.error(loc, message);
  }

  public FunctionFile parse() {
    FunctionFile file = new FunctionFile();
    file.setPosition(new Location(1, 0, 0));
    statementList(file, false);
    return file;
  }

  Statement statement() {
    Token peekedToken = peek();

    // Skip, '$' tells the lexer that this is not a command line
    if (peekedToken.is(DOLLAR_SIGN)) {
      next();
      peekedToken = peek();
    }

    TokenType peek = peekedToken.type();

    Statement s = switch (peek) {
      case CURLY_START -> block();
      case IF -> ifStatement();
      case LET, GLOBAL_LET, CONST -> variableDefinition();
      case FUNC -> function();
      case COMMAND_STRING -> commandExpr();
      case RETURN -> returnStatement();
      default -> exprStatement();
    };

    return s;
  }

  ReturnStatement returnStatement() {
    Token start = expect(RETURN);

    ReturnStatement stat = new ReturnStatement();
    stat.setPosition(start.location());

    if (peek().location().line() == start.location().line()
        && !matches(SEMICOLON, COMMAND_STRING)
    ) {
      Expression expr = assignExpr();
      stat.setValue(expr);
    }

    return stat;
  }

  FunctionStatement function() {
    Token start = expect(FUNC);

    FunctionStatement stat = new FunctionStatement();
    stat.setPosition(start.location());

    Identifier name = id();
    stat.setName(name);

    expect(PAREN_START);

    while (!matches(PAREN_CLOSE, EOF)) {
      Identifier paramName = id();
      stat.getParameters().add(paramName);

      if (matches(COMMA)) {
        next();
      } else if (matches(PAREN_CLOSE)) {
        break;
      } else {
        expect(PAREN_CLOSE, COMMA);
      }
    }

    expect(PAREN_CLOSE);
    stat.setBody(block());

    return stat;
  }

  ExprStatement exprStatement() {
    Expression expr = assignExpr();
    ExprStatement stat = new ExprStatement();
    stat.setExpr(expr);
    stat.setPosition(expr.getPosition());
    return stat;
  }

  Block block() {
    Token start = expect(CURLY_START);

    Block block = new Block();
    block.setPosition(start.location());

    statementList(block, true);

    expect(CURLY_CLOSE);
    return block;
  }

  void statementList(Block out, boolean expectCurly) {
    TokenType[] types;

    if (expectCurly) {
      types = new TokenType[]{EOF, CURLY_CLOSE};
    } else {
      types = new TokenType[]{EOF};
    }

    while (!matches(types)) {
      Statement stat = statement();

      if (stat instanceof FunctionStatement func) {
        out.getFunctions().add(func);
      } else {
        out.getBody().add(stat);
      }
    }
  }

  VariableDefinition variableDefinition() {
    Token start = expect(LET, GLOBAL_LET, CONST);

    VariableDefinition.Type type = switch (start.type()) {
      case LET -> Type.REGULAR;
      case GLOBAL_LET -> Type.GLOBAL;
      default -> Type.CONST;
    };

    Identifier id = id();
    Expression value;

    if (matches(ASSIGN)) {
      next();
      value = assignExpr();
    } else {
      value = null;
    }

    VariableDefinition definition = new VariableDefinition();
    definition.setPosition(start.location());
    definition.setType(type);
    definition.setName(id);
    definition.setValue(value);

    return definition;
  }

  IfStatement ifStatement() {
    Token token = expect(IF);

    expect(PAREN_START);
    Expression expr = assignExpr();
    expect(PAREN_CLOSE);

    Block block = block();
    Statement elseStat;

    if (matches(DOLLAR_SIGN)) {
      next();
    }

    if (matches(ELSE)) {
      next();
      elseStat = statement();
    } else {
      elseStat = null;
    }

    IfStatement stat = new IfStatement();
    stat.setPosition(token.location());
    stat.setBody(block);
    stat.setCondition(expr);
    stat.setElseStatement(elseStat);

    return stat;
  }

  CommandStatement commandExpr() {
    CommandStatement expr = new CommandStatement();
    Token token = expect(COMMAND_STRING);
    expr.setPosition(token.location());
    expr.setCommand(token.value());
    return expr;
  }

  Identifier id() {
    Token idToken = expect(ID);
    Identifier id = new Identifier();
    id.setPosition(idToken.location());
    id.setValue(idToken.value());
    return id;
  }

  Expression assignExpr() {
    var primary = primaryExpr();
    return callExpr(primary);
  }

  Expression primaryExpr() {
    Token peeked = peek();
    TokenType type = peeked.type();

    return switch (type) {

      // Literals
      case NULL           -> nullLiteral();
      case TRUE, FALSE    -> booleanLiteral();
      case QUOTED_STRING  -> stringLiteral();
      case HEX,
          BINARY,
          OCTAL,
          NUMBER          -> numberLiteral();

      default             -> id();
    };
  }

  Expression callExpr(Expression target) {
    if (!matches(PAREN_START)) {
      return target;
    }

    CallExpr expr = new CallExpr();
    expr.setPosition(target.getPosition());
    expr.setTarget(target);

    expect(PAREN_START);

    while (!matches(PAREN_CLOSE, EOF)) {
      Expression arg = assignExpr();
      expr.getArguments().add(arg);

      if (matches(COMMA)) {
        next();
      } else if (matches(PAREN_CLOSE)) {
        break;
      } else {
        expect(PAREN_CLOSE, COMMA);
      }
    }

    expect(PAREN_CLOSE);
    return expr;
  }

  NullLiteral nullLiteral() {
    Token token = expect(NULL);
    NullLiteral literal = new NullLiteral();
    literal.setPosition(token.location());
    return literal;
  }

  BooleanLiteral booleanLiteral() {
    Token token = expect(TRUE, FALSE);
    BooleanLiteral bool = new BooleanLiteral();
    bool.setValue(token.is(TRUE));
    bool.setPosition(token.location());
    return bool;
  }

  StringLiteral stringLiteral() {
    Token token = expect(QUOTED_STRING);
    StringLiteral literal = new StringLiteral();
    literal.setValue(token.value());
    literal.setPosition(token.location());
    return literal;
  }

  NumberLiteral numberLiteral() {
    Token token = expect(OCTAL, BINARY, HEX, NUMBER);

    NumberLiteral literal = new NumberLiteral();
    literal.setPosition(token.location());

    String val = token.value();

    Number num = switch (token.type()) {
      case BINARY -> Long.parseLong(val, 2);
      case HEX -> Long.parseLong(val, 16);
      case OCTAL -> Long.parseLong(val, 8);
      default -> Double.parseDouble(val);
    };

    literal.setNumber(num);

    return literal;
  }
}
