package me.jules.mcfl;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class Errors {

  private final StringBuffer input;

  private final List<Entry> entries = new ArrayList<>();

  @Getter @Setter
  private boolean throwErrors = true;

  public Errors(StringBuffer input) {
    this.input = input;
  }

  public List<Entry> getEntries() {
    return entries;
  }

  public void error(Location loc, String message, Object... args) {
    add(loc, message.formatted(args), Level.ERROR);
  }

  public void warn(Location loc, String message, Object... args) {
    add(loc, message.formatted(args), Level.WARN);
  }

  private void add(Location loc, String message, Level level) {
    Entry entry = new Entry(message, loc, level);
    entries.add(entry);

    if (level == Level.ERROR && throwErrors) {
      String fullMessage = entry.contextualMessage(input);
      throw new ParseException(fullMessage);
    }
  }

  public record Entry(String message, Location location, Level level) {

    String contextualMessage(StringBuffer in) {
      return ErrorMessages.format(in, location, message);
    }
  }

  public enum Level {
    WARN,
    ERROR
  }
}
