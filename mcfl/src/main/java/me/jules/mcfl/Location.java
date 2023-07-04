package me.jules.mcfl;

public record Location(int line, int column, int cursor) {

  @Override
  public String toString() {
    return line + ":" + column;
  }
}
