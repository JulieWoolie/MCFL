package me.jules.mcfl.ast;

import lombok.Getter;
import me.jules.mcfl.interpreter.Scope;

@Getter
public class FunctionFile extends Block {

  @Override
  public <R, C> R visit(NodeVisitor<R, C> visitor, C context) {
    return visitor.visitRoot(this, context);
  }

  @Override
  protected Scope createChild(Scope scope) {
    return scope;
  }
}
