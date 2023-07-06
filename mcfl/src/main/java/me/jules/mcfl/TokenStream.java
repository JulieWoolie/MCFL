package me.jules.mcfl;

import static me.jules.mcfl.TokenType.ADD;
import static me.jules.mcfl.TokenType.AND;
import static me.jules.mcfl.TokenType.ASSIGN;
import static me.jules.mcfl.TokenType.ASSIGN_ADD;
import static me.jules.mcfl.TokenType.ASSIGN_AND;
import static me.jules.mcfl.TokenType.ASSIGN_DIV;
import static me.jules.mcfl.TokenType.ASSIGN_MOD;
import static me.jules.mcfl.TokenType.ASSIGN_MUL;
import static me.jules.mcfl.TokenType.ASSIGN_OR;
import static me.jules.mcfl.TokenType.ASSIGN_POW;
import static me.jules.mcfl.TokenType.ASSIGN_SHIFT_LEFT;
import static me.jules.mcfl.TokenType.ASSIGN_SHIFT_RIGHT;
import static me.jules.mcfl.TokenType.ASSIGN_SUB;
import static me.jules.mcfl.TokenType.ASSIGN_USHIFT_LEFT;
import static me.jules.mcfl.TokenType.ASSIGN_USHIFT_RIGHT;
import static me.jules.mcfl.TokenType.ASSIGN_XOR;
import static me.jules.mcfl.TokenType.BINARY;
import static me.jules.mcfl.TokenType.COMMA;
import static me.jules.mcfl.TokenType.CURLY_CLOSE;
import static me.jules.mcfl.TokenType.CURLY_START;
import static me.jules.mcfl.TokenType.DECREMENT;
import static me.jules.mcfl.TokenType.DIV;
import static me.jules.mcfl.TokenType.DOLLAR_SIGN;
import static me.jules.mcfl.TokenType.DOT;
import static me.jules.mcfl.TokenType.EQUALS;
import static me.jules.mcfl.TokenType.GT;
import static me.jules.mcfl.TokenType.GTE;
import static me.jules.mcfl.TokenType.HEX;
import static me.jules.mcfl.TokenType.ID;
import static me.jules.mcfl.TokenType.INCREMENT;
import static me.jules.mcfl.TokenType.INVERT;
import static me.jules.mcfl.TokenType.LOOP_LABEL;
import static me.jules.mcfl.TokenType.LT;
import static me.jules.mcfl.TokenType.LTE;
import static me.jules.mcfl.TokenType.MOD;
import static me.jules.mcfl.TokenType.MUL;
import static me.jules.mcfl.TokenType.NEGATE;
import static me.jules.mcfl.TokenType.NON_TEMPLATED_COMMAND;
import static me.jules.mcfl.TokenType.NON_TEMPLATED_STRING;
import static me.jules.mcfl.TokenType.NUMBER;
import static me.jules.mcfl.TokenType.N_EQUALS;
import static me.jules.mcfl.TokenType.OCTAL;
import static me.jules.mcfl.TokenType.OR;
import static me.jules.mcfl.TokenType.PAREN_CLOSE;
import static me.jules.mcfl.TokenType.PAREN_START;
import static me.jules.mcfl.TokenType.POW;
import static me.jules.mcfl.TokenType.SEMICOLON;
import static me.jules.mcfl.TokenType.SHIFT_LEFT;
import static me.jules.mcfl.TokenType.SHIFT_RIGHT;
import static me.jules.mcfl.TokenType.SUB;
import static me.jules.mcfl.TokenType.TEMPLATE_CMD_HEAD;
import static me.jules.mcfl.TokenType.TEMPLATE_CMD_PART;
import static me.jules.mcfl.TokenType.TEMPLATE_CMD_TAIL;
import static me.jules.mcfl.TokenType.TEMPLATE_EXPR_START;
import static me.jules.mcfl.TokenType.TEMPLATE_ID_START;
import static me.jules.mcfl.TokenType.TEMPLATE_STRING_HEAD;
import static me.jules.mcfl.TokenType.TEMPLATE_STRING_PART;
import static me.jules.mcfl.TokenType.TEMPLATE_STRING_TAIL;
import static me.jules.mcfl.TokenType.UNKNOWN;
import static me.jules.mcfl.TokenType.USHIFT_LEFT;
import static me.jules.mcfl.TokenType.USHIFT_RIGHT;
import static me.jules.mcfl.TokenType.XOR;

import java.util.function.IntPredicate;

public class TokenStream {

  public static final int EOF     = -1;
  public static final int NO_CHAR = -2;
  public static final int LF = '\n';

  public static final int TEMPLATE_CHAR = '$';

  public static final int NOT_IN_TEMPLATE = -2;
  public static final int TP_NONE = -1;
  public static final int TP_ID = 0;
  public static final int TP_EXPR = 1;

  private final StringBuffer input;

  private final Errors errors;

  private int cursor        = 0;
  private int line          = 1;
  private int col           = 0;

  private int currentChar = NO_CHAR;

  private Location tokenLocation;
  private Token peekedToken;

  private final StringBuffer readbuf = new StringBuffer();

  // Used in debugging to know where the heck this reader is at in the input
  private final StringBuffer contextBuffer = new StringBuffer();

  boolean commandsForbidden = false;

  private boolean insideQuotedString;
  private int quoteChar = EOF;

  private boolean insideCommand;
  private boolean insideTemplate;
  private int templateType = NOT_IN_TEMPLATE;

  public TokenStream(StringBuffer input) {
    this.input = input;
    this.errors = new Errors(input);

    normalizeNewline(input);

    cursor = -1;
    advance();
  }

  private static void normalizeNewline(StringBuffer buf) {
    int cursor = 0;

    while (cursor < buf.length()) {
      char ch = buf.charAt(cursor);

      if (ch == '\r') {
        buf.setCharAt(cursor, (char) LF);
        int nIndex = cursor + 1;

        if (cursor < buf.length() - 1 && buf.charAt(nIndex) == LF) {
          buf.deleteCharAt(nIndex);
        }
      }

      cursor++;
    }
  }

  public StringBuffer input() {
    return input;
  }

  public Location location() {
    return new Location(line, col, cursor);
  }

  public int peek() {
    return peek(1);
  }

  public int peek(int ahead) {
    return charAt(cursor + ahead);
  }

  public void advance(int adv) {
    for (int i = 0; i < adv; i++) {
      advance();
    }
  }

  public int advance() {
    int nCursor = cursor + 1;

    if (nCursor >= input.length()) {
      currentChar = EOF;
      cursor = nCursor;

      return EOF;
    }

    int nChar = charAt(nCursor);

    if (nChar == LF || nChar == '\r') {
      line++;
      col = 0;

      if (nChar == '\r' && charAt(nCursor + 1) == '\n') {
        nCursor++;
      }

      // Normalize all line breaks to LF
      nChar = LF;
    } else {
      col++;
    }

    cursor = nCursor;
    currentChar = nChar;

    remakeContextBuffer();

    return currentChar;
  }

  private void remakeContextBuffer() {
    int cMin = Math.max(0, cursor - 10);
    int cMax = Math.min(input.length(), cursor + 10);

    if (!contextBuffer.isEmpty()) {
      contextBuffer.delete(0, contextBuffer.length());
    }

    for (int i = cMin; i < cMax; i++) {
      if (i == cursor) {
        contextBuffer.append('|');
      }

      int ch = charAt(i);
      contextBuffer.appendCodePoint(ch);
    }
  }

  private void clearReadbuf() {
    if (readbuf.length() < 1) {
      return;
    }

    readbuf.delete(0, readbuf.length());
  }

  private int charAt(int index) {
    if (index < 0 || index >= input.length()) {
      return EOF;
    }

    return input.charAt(index);
  }

  public String readIdentifier() {
    clearReadbuf();

    while (Character.isJavaIdentifierPart(currentChar)) {
      readbuf.appendCodePoint(currentChar);
      advance();
    }

    return readbuf.toString();
  }

  public void skipIgnorable() {
    while (true) {
      if (currentChar == EOF) {
        break;
      }

      if (Character.isWhitespace(currentChar)) {
        advance();
        continue;
      }

      if (currentChar == '#') {
        skipLineComment();
        continue;
      }

      if (currentChar == '/' && peek() == '*') {
        skipBlockComment();
        continue;
      }

      break;
    }
  }

  private void skipLineComment() {
    do {
      advance();
    } while (currentChar != LF && currentChar != EOF);
  }

  private void skipBlockComment() {
    while (true) {
      advance();

      if (currentChar == '*' && peek() == '/') {
        advance(2);
        break;
      }
    }
  }


  private String readHex() {
    return readNumeric("hex", TokenStream::isHexChar);
  }

  private String readOctal() {
    return readNumeric("octal", ch -> ch >= '0' && ch <= '7');
  }

  private String readBinary() {
    return readNumeric("binary", ch -> ch == '0' || ch == '1');
  }

  private String readSign() {
    if (currentChar == '-') {
      advance();
      return "-";
    }

    if (currentChar == '+') {
      advance();
    }

    return "";
  }

  private String readNumber() {
    clearReadbuf();

    Location loc = location();
    boolean decimalPointSet = false;
    boolean exponentSet = false;
    boolean lastWasUnderscore = false;

    while (true) {
      if (currentChar >= '0' && currentChar <= '9') {
        readbuf.appendCodePoint(currentChar);
        advance();
        continue;
      }

      if (currentChar == '.') {
        if (decimalPointSet) {
          errors.error(loc, "Invalid number sequence (Decimal point set twice)");
        }

        advance();
        decimalPointSet = true;
        readbuf.append('.');

        continue;
      }

      if (currentChar == 'e' || currentChar == 'E') {
        if (exponentSet) {
          errors.error(loc, "Invalid number sequence (Exponent set twice)");
        }

        advance();
        readbuf.append('e');

        if (currentChar == '+') {
          advance();
          readbuf.append("+");
        } else if (currentChar == '-') {
          advance();
          readbuf.append("-");
        } else {
          errors.error(location(), "Exponent character must be followed by minus or plus sign");
        }

        exponentSet = true;
        continue;
      }

      if (currentChar == '_') {
        if (lastWasUnderscore) {
          errors.error(location(), "Double underscore in number sequence");
        }

        if (loc.cursor() == cursor) {
          errors.error(loc, "Invalid starting character for number");
        }

        lastWasUnderscore = true;
        advance();

        continue;
      }

      break;
    }

    if (lastWasUnderscore) {
      errors.error(loc, "Invalid number sequence ending character ('_')");
    }

    if (readbuf.isEmpty()) {
      errors.error(loc, "Invalid number sequence");
    }

    return readbuf.toString();
  }

  private String readNumeric(String sequenceName, IntPredicate predicate) {
    clearReadbuf();

    var loc = location();
    final int start = cursor;
    boolean lastWasUnderscore = false;

    while (predicate.test(currentChar) || currentChar == '_') {
      if (currentChar == '_') {
        if (cursor == start) {
          errors.error(location(), "Invalid %s start", sequenceName);
        }

        lastWasUnderscore = true;
        advance();

        continue;
      }

      readbuf.appendCodePoint(currentChar);
      advance();
    }

    if (lastWasUnderscore) {
      errors.error(location(), "Invalid %s ending character", sequenceName);
    }

    if (readbuf.isEmpty()) {
      errors.error(loc, "Invalid %s sequence", sequenceName);
    }

    return readbuf.toString();
  }

  private static boolean isHexChar(int ch) {
    return (ch >= '0' && ch <= '9')
        || (ch >= 'a' && ch <= 'f')
        || (ch >= 'A' && ch <= 'F');
  }

  private static boolean isNumberStart(int ch) {
    return (ch >= '0' && ch <= '9')
        || ch == '-'
        || ch == '+'
        || ch == '.';
  }

  public Token nextToken() {
    if (peekedToken != null) {
      var t = peekedToken;
      peekedToken = null;
      return t;
    }

    return readToken();
  }

  public Token peekToken() {
    if (peekedToken != null) {
      return peekedToken;
    }

    peekedToken = readToken();
    return peekedToken;
  }

  private Token readToken() {
    if (!insideTemplate) {
      if (insideQuotedString) {
        return readQuotedString(false);
      } else if (insideCommand) {
        return readCommand(false);
      }
    } else if (templateType == TP_ID) {
      tokenLocation = location();
      String id = readIdentifier();

      if (id.isEmpty()) {
        errors.error(location(), "Expected template character '$' to be followed by identifier");
      }

      TokenType type = findKeyword(id);
      Token t;

      if (type != null) {
        t = token(type);
      } else {
        t = token(ID, id);
      }

      templateType = NOT_IN_TEMPLATE;
      insideTemplate = false;

      return t;
    }

    skipIgnorable();
    tokenLocation = location();

    if (currentChar == EOF) {
      return token(TokenType.EOF);
    }

    return switch (currentChar) {
      case '{' -> singleCharToken(CURLY_START);
      case '(' -> singleCharToken(PAREN_START);
      case ')' -> singleCharToken(PAREN_CLOSE);
      case ',' -> singleCharToken(COMMA);
      case '.' -> singleCharToken(DOT);
      case ';' -> singleCharToken(SEMICOLON);
      case '~' -> singleCharToken(INVERT);

      case '!' -> charOrAssign(NEGATE, N_EQUALS);
      case '=' -> charOrAssign(ASSIGN, EQUALS);

      case '^' -> charOrAssign(XOR, ASSIGN_XOR);
      case '/' -> charOrAssign(DIV, ASSIGN_DIV);
      case '%' -> charOrAssign(MOD, ASSIGN_MOD);

      case '}' -> {
        if (insideTemplate) {
          insideTemplate = false;
          templateType = NOT_IN_TEMPLATE;
        }
        yield singleCharToken(CURLY_CLOSE);
      }

      case '$' -> {
        advance();

        if (templateType == TP_NONE) {
          insideTemplate = true;

          if (currentChar == '{') {
            advance();
            templateType = TP_EXPR;
            yield token(TEMPLATE_EXPR_START);
          }

          templateType = TP_ID;
          yield token(TEMPLATE_ID_START);
        }

        String id = readIdentifier();

        if (id.isEmpty()) {
          yield token(DOLLAR_SIGN);
        }

        yield identifierToken(id);
      }

      case '*' -> {
        advance();

        if (currentChar == '*') {
          advance();

          if (currentChar == '=') {
            yield token(ASSIGN_POW);
          }

          yield token(POW);
        }

        if (currentChar == '=') {
          advance();
          yield token(ASSIGN_MUL);
        }

        yield token(MUL);
      }

      case '+' -> {
        advance();

        if (currentChar == '+') {
          advance();
          yield token(INCREMENT);
        }

        if (currentChar == '=') {
          advance();
          yield token(ASSIGN_ADD);
        }

        yield token(ADD);
      }

      case '-' -> {
        advance();

        if (currentChar == '-') {
          advance();
          yield token(DECREMENT);
        }

        if (currentChar == '=') {
          advance();
          yield token(ASSIGN_SUB);
        }

        yield token(SUB);
      }

      case '|' -> {
        advance();

        if (currentChar == '|') {
          advance();

          if (currentChar == '=') {
            advance();
            yield token(ASSIGN_OR);
          }

          yield token(OR);
        }

        if (currentChar == '=') {
          advance();
          yield token(ASSIGN_OR);
        }

        yield token(OR);
      }

      case '&' -> {
        advance();

        if (currentChar == '&') {
          advance();

          if (currentChar == '=') {
            advance();
            yield token(ASSIGN_AND);
          }

          yield token(AND);
        }

        if (currentChar == '=') {
          advance();
          yield token(ASSIGN_AND);
        }

        yield token(AND);
      }

      case '<' -> {
        advance();

        if (currentChar == '<') {
          advance();

          if (currentChar == '=') {
            advance();
            yield token(ASSIGN_SHIFT_LEFT);
          }

          if (currentChar == '<') {
            advance();

            if (currentChar == '=') {
              advance();
              yield token(ASSIGN_USHIFT_LEFT);
            }

            yield token(USHIFT_LEFT);
          }

          yield token(SHIFT_LEFT);
        }

        if (currentChar == '=') {
          advance();
          yield token(LTE);
        }

        yield token(LT);
      }

      case '>' -> {
        advance();

        if (currentChar == '>') {
          advance();

          if (currentChar == '=') {
            advance();
            yield token(ASSIGN_SHIFT_RIGHT);
          }

          if (currentChar == '>') {
            advance();

            if (currentChar == '=') {
              advance();
              yield token(ASSIGN_USHIFT_RIGHT);
            }

            yield token(USHIFT_RIGHT);
          }

          yield token(SHIFT_RIGHT);
        }

        if (currentChar == '=') {
          advance();
          yield token(GTE);
        }

        yield token(GT);
      }

      case '`', '\'', '"' -> {
        yield readQuotedString(true);
      }

      default -> {
        if (canBeCommandString()) {
          yield readCommand(true);
        }

        if (isNumberStart(currentChar)) {
          yield numberToken();
        }

        yield idToken();
      }
    };
  }

  private Token readQuotedString(boolean beginning) {
    if (beginning) {
      int quoted = currentChar;

      if (quoted != '"' && quoted != '`' && quoted != '\'') {
        errors.error(location(), "Invalid quote %c", quoted);
      }

      advance();

      quoteChar = quoted;
      insideQuotedString = true;
    }

    String str = readUntilQuoteOrTemplate();

    if (currentChar == TEMPLATE_CHAR) {
      insideTemplate = true;
      templateType = TP_NONE;

      if (beginning) {
        return token(TEMPLATE_STRING_HEAD, str);
      } else {
        return token(TEMPLATE_STRING_PART, str);
      }
    } else if (currentChar != quoteChar) {
      // ??????
      errors.error(location(), "Invalid string stop");
    }

    advance();
    insideQuotedString = false;
    quoteChar = EOF;

    if (beginning) {
      return token(NON_TEMPLATED_STRING, str);
    } else {
      return token(TEMPLATE_STRING_TAIL, str);
    }
  }

  private Token readCommand(boolean beginning) {
    if (beginning) {
      skipIgnorable();
      if (currentChar == ';') {
        advance();
      }

      skipIgnorable();
      insideCommand = true;
    }

    String str = readCommandPart();

    if (currentChar == TEMPLATE_CHAR) {
      insideTemplate = true;
      templateType = TP_NONE;

      if (beginning) {
        return token(TEMPLATE_CMD_HEAD, str);
      } else {
        return token(TEMPLATE_CMD_PART, str);
      }
    } else if (!isCommandEnd(currentChar)) {
      //?????
      errors.error(location(), "Invalid command end");
    }

    insideCommand = false;
    advance();

    if (beginning) {
      return token(NON_TEMPLATED_COMMAND, str);
    } else {
      return token(TEMPLATE_CMD_TAIL, str);
    }
  }

  private boolean canBeCommandString() {
    if (commandsForbidden) {
      return false;
    }

    int lineStart = findLineStart();
    int cursor = this.cursor;

    if (lineStart == cursor) {
      return true;
    }

    String substring = input.substring(lineStart, cursor);
    return substring.isBlank();
  }

  int findLineStart() {
    int r = cursor;

    while (r >= 0 && r < input.length()) {
      int c = charAt(r);

      if (c == '\n' || c == ';') {
        return r + 1;
      }

      r--;
    }

    return Math.max(0, r);
  }

  private static boolean isCommandEnd(int ch) {
    return ch == LF || ch == EOF || ch == ';';
  }

  private String readCommandPart() {
    clearReadbuf();

    boolean escaped = false;

    while (currentChar != EOF && currentChar != LF) {
      if (currentChar == '\\') {
        if (escaped) {
          readbuf.append("\\");
        }

        escaped = true;
        advance();
        continue;
      }

      if (currentChar == ';') {
        if (escaped) {
          readbuf.append(";");
          advance();
          escaped = false;
          continue;
        }

        advance();
        break;
      }

      if (currentChar == TEMPLATE_CHAR) {
        if (escaped) {
          readbuf.appendCodePoint(TEMPLATE_CHAR);
          escaped = false;
          advance();
          continue;
        }

        int peek = peek();

        if (peek == '{' || Character.isJavaIdentifierStart(peek)) {
          break;
        }
      }

      if (escaped) {
        readbuf.append("\\");
        escaped = false;
      }

      readbuf.appendCodePoint(currentChar);
      advance();
    }

    if (escaped) {
      readbuf.append("\\");
    }

    return readbuf.toString();
  }

  private String readUntilQuoteOrTemplate() {
    clearReadbuf();
    boolean escaped = false;

    Location start = location();

    while (true) {

      if (currentChar == EOF) {
        errors.error(location(), "End-Of-File inside quoted string");
      }

      if (currentChar == LF) {
        errors.error(start, "Line break inside quoted string");
      }

      if (currentChar == '\\') {
        advance();

        if (escaped) {
          readbuf.append("\\");
          escaped = false;
          continue;
        }

        escaped = true;
        continue;
      }

      if (currentChar == quoteChar) {
        if (escaped) {
          escaped = false;
          readbuf.appendCodePoint(quoteChar);
          advance();
          continue;
        }

        break;
      }

      if (currentChar == TEMPLATE_CHAR) {
        if (escaped) {
          readbuf.appendCodePoint(TEMPLATE_CHAR);
          escaped = false;
          advance();
          continue;
        }

        int peek = peek();

        if (peek == '{' || Character.isJavaIdentifierStart(peek)) {
          break;
        }
      }

      if (escaped) {
        switch (currentChar) {
          case 'u' -> {
            int unicode = readUnicodeCharacter();
            readbuf.appendCodePoint(unicode);
          }

          case 'n' -> readbuf.append("\n");
          case 'r' -> readbuf.append("\r");
          case 't' -> readbuf.append("\t");
          case 'b' -> readbuf.append("\b");
          case 'f' -> readbuf.append("\f");

          default -> {
            errors.error(location(), "Invalid escape character");
            advance();
          }
        }

        escaped = false;
        continue;
      }

      readbuf.appendCodePoint(currentChar);
      advance();
    }

    return readbuf.toString();
  }

  private int readUnicodeCharacter() {
    StringBuffer buf = new StringBuffer();
    Location start = location();

    while (isHexChar(currentChar)) {
      buf.appendCodePoint(currentChar);
      advance();
    }

    if (buf.length() < 4) {
      errors.error(start, "Invalid unicode sequence (must be 4 hex characters)");
    }

    return Integer.parseUnsignedInt(buf, 0, 4, 16);
  }

  private Token charOrAssign(TokenType lone, TokenType assign) {
    advance();

    if (currentChar == '=') {
      advance();
      return token(assign);
    }

    return token(lone);
  }

  private Token numberToken() {
    String sign = readSign();

    if (currentChar == '0') {
      if (peek() == 'x' || peek() == 'X') {
        advance(2);
        String hex = readHex();
        return token(HEX, sign + hex);
      }

      if (peek() == 'o' || peek() == 'O') {
        advance(2);
        String octal = readOctal();
        return token(OCTAL, sign + octal);
      }

      if (peek() == 'b' || peek() == 'B') {
        advance(2);
        String binary = readBinary();
        return token(BINARY, sign + binary);
      }
    }

    String number = readNumber();
    return token(NUMBER, sign + number);
  }

  private Token idToken() {
    String id = readIdentifier();

    if (id.isEmpty()) {
      advance();
      return token(UNKNOWN);
    }

    return identifierToken(id);
  }

  private Token identifierToken(String id) {
    TokenType keyword = findKeyword(id);
    if (keyword != null) {
      return token(keyword);
    }

    if (currentChar == ':') {
      advance();
      return token(LOOP_LABEL, id);
    }

    return token(ID, id);
  }

  private TokenType findKeyword(String name) {
    return TokenType.KEYWORDS.get(name);
  }

  private Token token(TokenType type) {
    return token(type, null);
  }

  private Token token(TokenType type, String value) {
    return new Token(type, value, tokenLocation);
  }

  private Token singleCharToken(TokenType type) {
    advance();
    return token(type);
  }

  public Errors errors() {
    return errors;
  }
}