package me.jules.mcfl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum TokenType {

  DOLLAR_SIGN         ("$"),
  COMMA               (","),
  SEMICOLON           (";"),
  DOT                 ("."),

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
  DEBUGGER            ("$debugger"),
  THROW               ("$throw"),
  FOR                 ("$for"),
  DO                  ("$do"),
  WHILE               ("$while"),

  ASSIGN              ("="),
  EQUALS              ("=="),
  N_EQUALS            ("!="),
  LT                  ("<"),
  LTE                 ("<="),
  GT                  (">"),
  GTE                 (">="),
  SHIFT_LEFT          ("<<"),
  USHIFT_LEFT         ("<<<"),
  SHIFT_RIGHT         (">>"),
  USHIFT_RIGHT        (">>>"),
  MOD                 ("%"),
  POW                 ("**"),
  ADD                 ("+"),
  SUB                 ("-"),
  MUL                 ("*"),
  DIV                 ("/"),
  OR                  ("|"),
  XOR                 ("^"),
  AND                 ("&"),
  INVERT              ("~"),
  NEGATE              ("!"),
  INCREMENT           ("++"),
  DECREMENT           ("--"),

  ASSIGN_SHIFT_LEFT   ("<<="),
  ASSIGN_USHIFT_LEFT  ("<<<="),
  ASSIGN_SHIFT_RIGHT  (">>="),
  ASSIGN_USHIFT_RIGHT (">>>="),
  ASSIGN_MOD          ("%="),
  ASSIGN_POW          ("**="),
  ASSIGN_ADD          ("+="),
  ASSIGN_SUB          ("-="),
  ASSIGN_MUL          ("*="),
  ASSIGN_DIV          ("/="),
  ASSIGN_OR           ("|="),
  ASSIGN_XOR          ("^="),
  ASSIGN_AND          ("&="),

  PAREN_START         ("("),
  PAREN_CLOSE         (")"),
  CURLY_START         ("{"),
  CURLY_CLOSE         ("}"),

  ID,
  LOOP_LABEL,

  NON_TEMPLATED_STRING,
  TEMPLATE_STRING_HEAD,
  TEMPLATE_STRING_PART,
  TEMPLATE_STRING_TAIL,

  NON_TEMPLATED_COMMAND,
  TEMPLATE_CMD_HEAD,
  TEMPLATE_CMD_PART,
  TEMPLATE_CMD_TAIL,

  TEMPLATE_EXPR_START ("${"),
  TEMPLATE_ID_START ("$"),

  NUMBER,
  HEX,
  OCTAL,
  BINARY,

  EOF,
  UNKNOWN,
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
    map.put("debugger",    DEBUGGER);
    map.put("throw",       THROW);
    map.put("for",         FOR);
    map.put("do",          DO);
    map.put("while",       WHILE);

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
