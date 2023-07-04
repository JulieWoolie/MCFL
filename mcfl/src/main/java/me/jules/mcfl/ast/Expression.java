package me.jules.mcfl.ast;

import me.jules.mcfl.Location;

public abstract class Expression extends Node {

  public Expression(Location position) {
    super(position);
  }

  public Expression() {
  }
}
