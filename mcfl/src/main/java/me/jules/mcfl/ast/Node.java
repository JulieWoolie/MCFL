package me.jules.mcfl.ast;

import me.jules.mcfl.Location;
import me.jules.mcfl.ScriptExecutable;

public abstract class Node implements ScriptExecutable {

  private Location position;

  public Node(Location position) {
    this.position = position;
  }

  public Node() {
  }

  public abstract <R, C> R visit(NodeVisitor<R, C> visitor, C context);

  public Location getPosition() {
    return position;
  }

  public void setPosition(Location position) {
    this.position = position;
  }
}
