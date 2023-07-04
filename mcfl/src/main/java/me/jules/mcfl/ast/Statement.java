package me.jules.mcfl.ast;

import me.jules.mcfl.Location;

public abstract class Statement extends Node {

  public Statement(Location position) {
    super(position);
  }

  public Statement() {
  }
}
