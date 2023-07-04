package me.jules.mcfl.ast;

import lombok.Getter;
import lombok.Setter;
import me.jules.mcfl.interpreter.ExecContext;
import me.jules.mcfl.interpreter.ReturnValue;
import me.jules.mcfl.interpreter.Scope;

@Getter @Setter
public class CommandStatement extends Statement {

  private String command;

  @Override
  public <R, C> R visit(NodeVisitor<R, C> visitor, C context) {
    return visitor.visitCommand(this, context);
  }

  @Override
  public ReturnValue execute(ExecContext ctx, Scope scope) {
    ctx.getExecutor().runComand(command);
    return ReturnValue.NO_RETURN;
  }

  @Override
  public String toString() {
    return "'" + command + "'";
  }
}
