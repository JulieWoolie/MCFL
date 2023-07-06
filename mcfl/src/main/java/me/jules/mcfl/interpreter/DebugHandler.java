package me.jules.mcfl.interpreter;

import me.jules.mcfl.Location;

public interface DebugHandler {

  void debugger(String message, Location location, ExecContext ctx);
}
