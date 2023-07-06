package me.jules.mcfl;

public final class ErrorMessages {
  private ErrorMessages() {}


  public static String format(StringBuffer input, Location location, String message) {
    if (location == null) {
      return message;
    }

    int pos = location.cursor();

    final int lineStart = findLineStart(input, pos);
    final int lineEnd = findLineEnd(input, pos);

    final int lineNumber = location.line();
    final int column = location.column();

    String context = input.substring(lineStart, lineEnd)
        .replace("\n", "")
        .replace("\r", "");

    String errorFormat = "%s\n%s\n%" + (Math.max(1, column)) + "s Line %s Column %s";

    return errorFormat.formatted(message, context, "^", lineNumber, column);
  }

  static int findLineStart(StringBuffer reader, int cursor) {
    return findLineEndStart(reader, cursor, -1);
  }

  static int findLineEnd(StringBuffer reader, int cursor) {
    return findLineEndStart(reader, cursor, 1);
  }

  static int findLineEndStart(
      StringBuffer reader,
      int pos,
      int direction
  ) {
    int r = pos;

    while (r >= 0 && r < reader.length()) {
      char c = reader.charAt(r);

      if (c == '\n' || c == '\r') {
        return r;
      }

      r += direction;
    }

    return Math.max(0, r);
  }
}
