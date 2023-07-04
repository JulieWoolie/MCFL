package me.jules.mcfl;

public record Token(TokenType type, String value, Location location) {

  public boolean is(TokenType type) {
    return this.type == type;
  }

  public boolean is(TokenType... types) {
    for (var t: types) {
      if (is(t)) {
        return true;
      }
    }

    return false;
  }
}
