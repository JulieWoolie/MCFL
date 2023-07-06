package me.jules.mcfl;

import static me.jules.mcfl.TokenType.ASSIGN;
import static me.jules.mcfl.TokenType.BINARY;
import static me.jules.mcfl.TokenType.BREAK;
import static me.jules.mcfl.TokenType.COMMA;
import static me.jules.mcfl.TokenType.CONST;
import static me.jules.mcfl.TokenType.CONTINUE;
import static me.jules.mcfl.TokenType.CURLY_CLOSE;
import static me.jules.mcfl.TokenType.CURLY_START;
import static me.jules.mcfl.TokenType.DEBUGGER;
import static me.jules.mcfl.TokenType.DECREMENT;
import static me.jules.mcfl.TokenType.DO;
import static me.jules.mcfl.TokenType.ELSE;
import static me.jules.mcfl.TokenType.EOF;
import static me.jules.mcfl.TokenType.FALSE;
import static me.jules.mcfl.TokenType.FOR;
import static me.jules.mcfl.TokenType.FUNC;
import static me.jules.mcfl.TokenType.GLOBAL_LET;
import static me.jules.mcfl.TokenType.HEX;
import static me.jules.mcfl.TokenType.ID;
import static me.jules.mcfl.TokenType.IF;
import static me.jules.mcfl.TokenType.INCREMENT;
import static me.jules.mcfl.TokenType.LET;
import static me.jules.mcfl.TokenType.LOOP_LABEL;
import static me.jules.mcfl.TokenType.NON_TEMPLATED_COMMAND;
import static me.jules.mcfl.TokenType.NON_TEMPLATED_STRING;
import static me.jules.mcfl.TokenType.NULL;
import static me.jules.mcfl.TokenType.NUMBER;
import static me.jules.mcfl.TokenType.OCTAL;
import static me.jules.mcfl.TokenType.PAREN_CLOSE;
import static me.jules.mcfl.TokenType.PAREN_START;
import static me.jules.mcfl.TokenType.RETURN;
import static me.jules.mcfl.TokenType.SEMICOLON;
import static me.jules.mcfl.TokenType.TEMPLATE_CMD_HEAD;
import static me.jules.mcfl.TokenType.TEMPLATE_CMD_PART;
import static me.jules.mcfl.TokenType.TEMPLATE_CMD_TAIL;
import static me.jules.mcfl.TokenType.TEMPLATE_EXPR_START;
import static me.jules.mcfl.TokenType.TEMPLATE_ID_START;
import static me.jules.mcfl.TokenType.TEMPLATE_STRING_HEAD;
import static me.jules.mcfl.TokenType.TEMPLATE_STRING_PART;
import static me.jules.mcfl.TokenType.TEMPLATE_STRING_TAIL;
import static me.jules.mcfl.TokenType.THROW;
import static me.jules.mcfl.TokenType.TRUE;
import static me.jules.mcfl.TokenType.WHILE;

import java.util.StringJoiner;
import me.jules.mcfl.ast.BinaryExpr;
import me.jules.mcfl.ast.BinaryOp;
import me.jules.mcfl.ast.Block;
import me.jules.mcfl.ast.BooleanLiteral;
import me.jules.mcfl.ast.CallExpr;
import me.jules.mcfl.ast.CommandStatement;
import me.jules.mcfl.ast.DebuggerStatement;
import me.jules.mcfl.ast.DoWhileStatement;
import me.jules.mcfl.ast.ExprStatement;
import me.jules.mcfl.ast.Expression;
import me.jules.mcfl.ast.ForStatement;
import me.jules.mcfl.ast.FunctionFile;
import me.jules.mcfl.ast.FunctionStatement;
import me.jules.mcfl.ast.Identifier;
import me.jules.mcfl.ast.IfStatement;
import me.jules.mcfl.ast.LoopFlowStatement;
import me.jules.mcfl.ast.LoopStatement;
import me.jules.mcfl.ast.NullLiteral;
import me.jules.mcfl.ast.NumberLiteral;
import me.jules.mcfl.ast.PropertyAccessExpr;
import me.jules.mcfl.ast.ReturnStatement;
import me.jules.mcfl.ast.StandardBinaryOp;
import me.jules.mcfl.ast.Statement;
import me.jules.mcfl.ast.StringLiteral;
import me.jules.mcfl.ast.TemplatedString;
import me.jules.mcfl.ast.TemplatedString.ExpressionPart;
import me.jules.mcfl.ast.TemplatedString.LiteralPart;
import me.jules.mcfl.ast.ThrowStatement;
import me.jules.mcfl.ast.UnaryExpr;
import me.jules.mcfl.ast.UnaryOp;
import me.jules.mcfl.ast.VariableDefinition;
import me.jules.mcfl.ast.VariableDefinition.Type;
import me.jules.mcfl.ast.WhileStatement;

public class Parser {

  private final TokenStream stream;

  private final Errors errors;

  private boolean insideLoop = false;
  private boolean returnAllowed = false;

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

    while (peekedToken.is(SEMICOLON)) {
      next();
      peekedToken = peek();
    }

    TokenType peek = peekedToken.type();

    return switch (peek) {
      case BREAK, CONTINUE -> loopFlowControl();
      case LET, GLOBAL_LET, CONST -> variableDefinition();
      case NON_TEMPLATED_COMMAND, TEMPLATE_CMD_HEAD -> commandExpr();

      case CURLY_START  -> block();
      case IF           -> ifStatement();
      case FOR          -> forLoop();
      case DO           -> doWhileLoop();
      case WHILE        -> whileLoop();
      case FUNC         -> function();
      case RETURN       -> returnStatement();
      case DEBUGGER     -> debugger();
      case THROW        -> throwStatement();
      case LOOP_LABEL   -> labelledStatement();

      default           -> exprStatement();
    };
  }

  LoopStatement labelledStatement() {
    Token label = expect(LOOP_LABEL);
    String labelValue = label.value();

    Identifier id = new Identifier();
    id.setValue(labelValue);
    id.setPosition(label.location());

    LoopStatement loop;

    if (matches(FOR)) {
      loop = forLoop();
    } else if (matches(DO)) {
      loop = doWhileLoop();
    } else if (matches(WHILE)) {
      loop = whileLoop();
    } else {
      expect(FOR, DO, WHILE);
      loop = null;
    }

    loop.setLabel(id);
    return loop;
  }

  WhileStatement whileLoop() {
    Token start = expect(WHILE);

    WhileStatement stat = new WhileStatement();
    stat.setPosition(start.location());
    stat.setCondition(parenthesizedExpr());
    stat.setBody(loopBlock());

    return stat;
  }

  DoWhileStatement doWhileLoop() {
    Token start = expect(DO);

    DoWhileStatement stat = new DoWhileStatement();
    stat.setPosition(start.location());
    stat.setBody(loopBlock());
    stat.setCondition(parenthesizedExpr());

    return stat;
  }

  ForStatement forLoop() {
    Token start = expect(FOR);
    expect(PAREN_START);

    ForStatement stat = new ForStatement();
    stat.setPosition(start.location());

    boolean forbid = stream.commandsForbidden;
    stream.commandsForbidden = true;

    if (!matches(SEMICOLON)) {
      VariableDefinition def = variableDefinition();
      stat.setFirst(def);
    }

    expect(SEMICOLON);

    if (!matches(SEMICOLON)) {
      Expression expr = expr();
      stat.setCondition(expr);
    }

    expect(SEMICOLON);

    if (!matches(PAREN_CLOSE)) {
      Expression expr = expr();
      stat.setThird(expr);
    }

    stream.commandsForbidden = forbid;

    expect(PAREN_CLOSE);
    stat.setBody(loopBlock());

    return stat;
  }

  private Block loopBlock() {
    boolean inLoop = insideLoop;

    insideLoop = true;
    Block block = block();
    insideLoop = inLoop;

    return block;
  }

  ThrowStatement throwStatement() {
    Token start = expect(THROW);

    ThrowStatement stat = new ThrowStatement();
    stat.setPosition(start.location());

    if (peek().location().line() == start.location().line()
        && !matches(SEMICOLON, NON_TEMPLATED_COMMAND, TEMPLATE_CMD_HEAD)
    ) {
      Expression expr = expr();
      stat.setMessage(expr);
    }

    if (matches(SEMICOLON)) {
      next();
    }

    return stat;
  }

  DebuggerStatement debugger() {
    Token start = expect(DEBUGGER);

    DebuggerStatement stat = new DebuggerStatement();
    stat.setPosition(start.location());

    if (peek().location().line() == start.location().line()
        && !matches(SEMICOLON, NON_TEMPLATED_COMMAND, TEMPLATE_CMD_HEAD)
    ) {
      Expression expr = expr();
      stat.setExpression(expr);
    }

    if (matches(SEMICOLON)) {
      next();
    }

    return stat;
  }

  ReturnStatement returnStatement() {
    Token start = expect(RETURN);

    ReturnStatement stat = new ReturnStatement();
    stat.setPosition(start.location());

    if (peek().location().line() == start.location().line()
        && !matches(SEMICOLON, NON_TEMPLATED_COMMAND, TEMPLATE_CMD_HEAD)
    ) {
      Expression expr = expr();
      stat.setValue(expr);
    }

    if (matches(SEMICOLON)) {
      next();
    }

    if (!returnAllowed) {
      error(start.location(), "Return statement now allowed here");
    }

    return stat;
  }

  LoopFlowStatement loopFlowControl() {
    Token token = expect(BREAK, CONTINUE);

    LoopFlowStatement.Type type = token.is(BREAK)
        ? LoopFlowStatement.Type.BREAK
        : LoopFlowStatement.Type.CONTINUE;

    Identifier label;

    if (matches(ID)) {
      label = id();
    } else {
      label = null;
    }

    if (matches(SEMICOLON)) {
      next();
    }

    LoopFlowStatement stat = new LoopFlowStatement();
    stat.setType(type);
    stat.setPosition(token.location());
    stat.setLabel(label);

    if (!insideLoop) {
      error(token.location(), "break/continue statement not allowed here");
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

    boolean retAllowed = returnAllowed;
    returnAllowed = true;

    stat.setBody(block());

    returnAllowed = retAllowed;

    return stat;
  }

  ExprStatement exprStatement() {
    Expression expr = expr();
    ExprStatement stat = new ExprStatement();
    stat.setExpr(expr);
    stat.setPosition(expr.getPosition());

    if (matches(SEMICOLON)) {
      next();
    }

    return stat;
  }

  Block block() {
    Token start = expect(CURLY_START);

    Block block = new Block();
    block.setPosition(start.location());

    statementList(block, true);

    if (insideLoop) {
      block.setLoopBlock(true);
    }

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
      value = expr();
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
    Expression expr = expr();
    expect(PAREN_CLOSE);

    Block block = block();
    Statement elseStat;

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
    TemplatedString string = new TemplatedString();

    expr.setPosition(peek().location());
    expr.setString(string);

    templated(
        string,
        NON_TEMPLATED_COMMAND,
        TEMPLATE_CMD_HEAD,
        TEMPLATE_CMD_PART,
        TEMPLATE_CMD_TAIL
    );

    return expr;
  }

  Identifier id() {
    Token idToken = expect(ID);
    Identifier id = new Identifier();
    id.setPosition(idToken.location());
    id.setValue(idToken.value());
    return id;
  }

  Expression expr() {
    return binaryExpr();
  }

  Expression binaryExpr() {
    Expression unary = unaryExpr();

    while (isBinaryOpToken()) {
      Token next = next();

      BinaryOp op = StandardBinaryOp.TOKEN_TO_OP.get(next.type());

      BinaryExpr expr = new BinaryExpr();
      expr.setPosition(unary.getPosition());
      expr.setOp(op);
      expr.setLhs(unary);
      expr.setRhs(unaryExpr());

      unary = expr;
    }

    return unary;
  }

  Expression unaryExpr() {
    Token peeked = peek();
    Location l = peeked.location();

    return switch (peeked.type()) {

      case NEGATE -> {
        next();
        yield createUnary(unaryExpr(), UnaryOp.NEGATE, l);
      }

      case INVERT -> {
        next();
        yield createUnary(unaryExpr(), UnaryOp.INVERT, l);
      }

      case INCREMENT -> {
        next();
        yield createUnary(unaryExpr(), UnaryOp.PRE_INC, l);
      }

      case DECREMENT -> {
        next();
        yield createUnary(unaryExpr(), UnaryOp.PRE_DEC, l);
      }

      case ADD -> {
        next();
        yield createUnary(unaryExpr(), UnaryOp.POSITIVE, l);
      }

      case SUB -> {
        next();
        yield createUnary(unaryExpr(), UnaryOp.NEGATIVE, l);
      }

      default -> {
        Expression member = memberExpr();

        if (!matches(INCREMENT, DECREMENT)) {
          yield member;
        }

        UnaryOp op = next().is(INCREMENT)
            ? UnaryOp.POST_INC
            : UnaryOp.POST_DEC;

        yield createUnary(member, op, l);
      }
    };
  }

  UnaryExpr createUnary(Expression expr, UnaryOp op, Location l) {
    UnaryExpr uExpr = new UnaryExpr();
    uExpr.setOperation(op);
    uExpr.setExpression(expr);
    uExpr.setPosition(l);
    return uExpr;
  }

  private boolean isBinaryOpToken() {
    Token peek = peek();
    return StandardBinaryOp.BINARY_OP_TOKENS.contains(peek.type());
  }

  Expression memberExpr() {
    Expression expr = primaryExpr();
    return memberTailExpr(expr);
  }

  Expression memberTailExpr(Expression parent) {
    Expression expr = parent;

    outer: while (true) {
      switch (peek().type()) {
        case PAREN_START -> {
          CallExpr callExpr = new CallExpr();
          callExpr.setPosition(expr.getPosition());
          callExpr.setTarget(expr);

          expect(PAREN_START);

          while (!matches(PAREN_CLOSE, EOF)) {
            Expression arg = expr();
            callExpr.getArguments().add(arg);

            if (matches(COMMA)) {
              next();
            } else if (matches(PAREN_CLOSE)) {
              break;
            } else {
              expect(PAREN_CLOSE, COMMA);
            }
          }

          expect(PAREN_CLOSE);
          expr = callExpr;
        }

        case DOT -> {
          next();
          Identifier id = id();

          PropertyAccessExpr access = new PropertyAccessExpr();
          access.setTarget(expr);
          access.setPosition(expr.getPosition());
          access.setPropertyName(id);

          expr = access;
        }

        default -> {
          break outer;
        }
      }
    }

    return expr;
  }

  Expression primaryExpr() {
    Token peeked = peek();
    TokenType type = peeked.type();

    return switch (type) {

      // Literals
      case NULL           -> nullLiteral();
      case TRUE, FALSE    -> booleanLiteral();

      case NON_TEMPLATED_STRING,
          TEMPLATE_STRING_HEAD -> stringLiteral();

      case HEX,
          BINARY,
          OCTAL,
          NUMBER          -> numberLiteral();

      case PAREN_START    -> parenthesizedExpr();

      case ID             -> id();

      default -> {
        error(peek().location(), "Expected expression, found %s", peek().type());
        yield id();
      }
    };
  }

  private Expression parenthesizedExpr() {
    expect(PAREN_START);
    Expression expr = expr();
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
    bool.value(token.is(TRUE));
    bool.setPosition(token.location());
    return bool;
  }

  StringLiteral stringLiteral() {
    StringLiteral literal = new StringLiteral();
    TemplatedString string = new TemplatedString();

    literal.setPosition(peek().location());
    literal.setString(string);

    templated(
        string,
        NON_TEMPLATED_STRING,
        TEMPLATE_STRING_HEAD,
        TEMPLATE_STRING_PART,
        TEMPLATE_STRING_TAIL
    );

    return literal;
  }

  void templated(
      TemplatedString str,
      TokenType nonTemplated,
      TokenType head,
      TokenType part,
      TokenType tail
  ) {
    Token token = expect(nonTemplated, head);

    LiteralPart headPart = new LiteralPart();
    headPart.setPosition(token.location());
    headPart.setValue(token.value());
    str.getParts().add(headPart);

    if (token.is(nonTemplated)) {
      return;
    }

    while (true) {
      Token n = next();

      if (n.is(part, tail)) {
        if (!n.value().isEmpty()) {
          LiteralPart literalPart = new LiteralPart();
          literalPart.setPosition(n.location());
          literalPart.setValue(n.value());

          str.getParts().add(literalPart);
        }

        if (n.is(part)) {
          continue;
        } else {
          break;
        }
      }

      if (n.is(TEMPLATE_ID_START, TEMPLATE_EXPR_START)) {
        Expression expr;

        if (n.is(TEMPLATE_ID_START)) {
          expr = id();
        } else {
          expr = expr();
          expect(CURLY_CLOSE);
        }

        ExpressionPart exprPart = new ExpressionPart();
        exprPart.setExpr(expr);
        exprPart.setPosition(expr.getPosition());

        str.getParts().add(exprPart);
        continue;
      }

      error(n.location(), "Unknown template token");
    }
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
