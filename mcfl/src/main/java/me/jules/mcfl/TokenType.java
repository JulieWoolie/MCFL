package me.jules.mcfl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum TokenType {

  DOLLAR_SIGN         ("$"),
  COMMA               (","),
  ASSIGN              ("="),
  SEMICOLON           (";"),

  LET                 ("$let"),
  CONST               ("$const"),
  GLOBAL_LET          ("$global"),
  NULL                ("null"),
  TRUE                ("true"),
  FALSE               ("false"),
  IF                  ("$if"),
  ELSE                ("$else"),
  FUNC                ("$function"),
  RETURN              ("$return"),
  BREAK               ("$break"),
  CONTINUE            ("$continue"),
  DELETE              ("$delete"),
  DEBUGGER            ("$debugger"),

  // Yet to be implemented
/*EQUALS              ("=="),
  N_EQUALS            ("!="),
  LT                  ("<"),
  LTE                 ("<="),
  GT                  (">"),
  GTE                 (">="),
  SHIFT_LEFT          (">>"),
  USHIFT_LEFT         (">>>"),
  SHIFT_RIGHT         ("<<"),
  USHIFT_RIGHT        ("<<<"),
  POW                 ("**"),
  ADD                 ("+"),
  SUB                 ("-"),
  MUL                 ("*"),
  DIV                 ("/"),

  ASSIGN_SHIFT_LEFT   (">>="),
  ASSIGN_USHIFT_LEFT  (">>>="),
  ASSIGN_SHIFT_RIGHT  ("<<="),
  ASSIGN_USHIFT_RIGHT ("<<<="),
  ASSIGN_POW          ("**="),
  ASSIGN_ADD          ("+="),
  ASSIGN_SUB          ("-="),
  ASSIGN_MUL          ("*="),
  ASSIGN_DIV          ("/="),

  NEGATE              ("!"),
  INCREMENT           ("++"),
  DECREMENT           ("--"),*/

  PAREN_START         ("("),
  PAREN_CLOSE         (")"),
  CURLY_START         ("{"),
  CURLY_CLOSE         ("}"),

  ID,
  QUOTED_STRING,
  COMMAND_STRING,

  NUMBER,
  HEX,
  OCTAL,
  BINARY,

  EOF,
  UNKNOWN,
  INVALID_COMMENT,
  ;

  public static final Map<String, TokenType> KEYWORDS;

  static {
    Map<String, TokenType> map = new HashMap<>();

    map.put("if",          IF);
    map.put("else",        ELSE);
    map.put("function",    FUNC);
    map.put("return",      RETURN);
    map.put("break",       BREAK);
    map.put("continue",    CONTINUE);
    map.put("let",         LET);
    map.put("const",       CONST);
    map.put("global",      GLOBAL_LET);
    map.put("true",        TRUE);
    map.put("false",       FALSE);
    map.put("null",        NULL);
    map.put("delete",      DELETE);
    map.put("debugger",    DEBUGGER);

    KEYWORDS = Collections.unmodifiableMap(map);
  }

  private final String stringValue;


  TokenType() {
    this(null);
  }

  TokenType(String stringValue) {
    this.stringValue = stringValue;
  }

  @Override
  public String toString() {
    if (stringValue != null) {
      return stringValue;
    }

    return name().toLowerCase();
  }
}
