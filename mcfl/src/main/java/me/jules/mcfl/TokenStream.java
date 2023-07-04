package me.jules.mcfl;

import static me.jules.mcfl.TokenType.ASSIGN;
import static me.jules.mcfl.TokenType.BINARY;
import static me.jules.mcfl.TokenType.COMMA;
import static me.jules.mcfl.TokenType.COMMAND_STRING;
import static me.jules.mcfl.TokenType.CURLY_CLOSE;
import static me.jules.mcfl.TokenType.CURLY_START;
import static me.jules.mcfl.TokenType.DOLLAR_SIGN;
import static me.jules.mcfl.TokenType.HEX;
import static me.jules.mcfl.TokenType.ID;
import static me.jules.mcfl.TokenType.NUMBER;
import static me.jules.mcfl.TokenType.OCTAL;
import static me.jules.mcfl.TokenType.PAREN_CLOSE;
import static me.jules.mcfl.TokenType.PAREN_START;
import static me.jules.mcfl.TokenType.QUOTED_STRING;
import static me.jules.mcfl.TokenType.SEMICOLON;
import static me.jules.mcfl.TokenType.UNKNOWN;

import java.util.Objects;
import java.util.function.IntPredicate;

public class TokenStream {

  public static final int EOF     = -1;
  public static final int NO_CHAR = -2;
  public static final int LF = '\n';

  private final StringBuffer input;

  private final Errors errors;

  private int cursor        = 0;
  private int line          = 1;
  private int col           = 0;

  private int currentChar = NO_CHAR;

  private Location tokenLocation;
  private Token peekedToken;

  StringBuffer readbuf = new StringBuffer();

  public TokenStream(StringBuffer input) {
    this.input = input;
    this.errors = new Errors(input);

    cursor = -1;
    advance();
  }

  public StringBuffer input() {
    return input;
  }

  public Location location() {
    return new Location(line, col, cursor);
  }

  public void setLocation(Location loc) {
    Objects.requireNonNull(loc);

    this.cursor = loc.cursor();
    this.line = loc.line();
    this.col = loc.column();
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

    return currentChar;
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
        advance();
        advance();

        break;
      }
    }
  }

  private String readCommandString() {
    skipIgnorable();
    if (currentChar == ';') {
      advance();
    }

    skipIgnorable();
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

  private String readQuotedString() {
    int quoted = currentChar;

    if (quoted != '"' && quoted != '`' && quoted != '\'') {
      errors.error(location(), "Invalid quote %c", quoted);
    }

    advance();
    clearReadbuf();

    boolean escaped = false;

    while (true) {

      if (currentChar == EOF) {
        errors.error(location(), "End-Of-File inside quoted string");
      }

      if (currentChar == LF) {
        errors.error(location(), "Line break inside quoted string");
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

      if (currentChar == quoted) {
        advance();

        if (escaped) {
          escaped = false;
          readbuf.appendCodePoint(quoted);
          continue;
        }

        break;
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
    skipIgnorable();
    tokenLocation = location();

    if (currentChar == EOF) {
      return token(TokenType.EOF);
    }

    return switch (currentChar) {
      case '{' -> singleCharToken(CURLY_START);
      case '}' -> singleCharToken(CURLY_CLOSE);
      case '(' -> singleCharToken(PAREN_START);
      case ')' -> singleCharToken(PAREN_CLOSE);
      case ',' -> singleCharToken(COMMA);
      case '=' -> singleCharToken(ASSIGN);
      case '$' -> singleCharToken(DOLLAR_SIGN);
      case ';' -> singleCharToken(SEMICOLON);

      case '`', '\'', '"' -> {
        String str = readQuotedString();
        yield token(QUOTED_STRING, str);
      }

      default -> {
        if (canBeCommandString()) {
          String str = readCommandString();
          yield token(COMMAND_STRING, str);
        }

        if (isNumberStart(currentChar)) {
          yield numberToken();
        }

        yield idToken();
      }
    };
  }

  private boolean canBeCommandString() {
    int lineStart = findLineStart();
    int lineEnd = ErrorMessages.findLineEnd(input, cursor);

    String substr = input.substring(lineStart, lineEnd).trim();

    if (substr.isEmpty()) {
      return false;
    }

    char first = substr.charAt(0);

    if (first == '$') {
      return false;
    }

    return first == '/' || Character.isJavaIdentifierPart(first);
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

    TokenType keyword = findKeyword(id);
    if (keyword != null) {
      return token(keyword);
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