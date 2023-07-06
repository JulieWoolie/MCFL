package me.jules.mcfl.ast;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import me.jules.mcfl.EvaluationError;
import me.jules.mcfl.interpreter.ExecContext;
import me.jules.mcfl.interpreter.ReturnValue;
import me.jules.mcfl.interpreter.Scope;
import me.jules.mcfl.interpreter.ValueRef;

@Getter
public class TemplatedString {

  private final List<Part> parts = new ArrayList<>();

  public String execute(ExecContext context, Scope scope) throws EvaluationError {
    if (parts.isEmpty()) {
      return "";
    }

    StringBuilder builder = new StringBuilder();

    for (Part n : parts) {
      ReturnValue value = n.execute(context, scope);
      ValueRef ref = value.ref();
      builder.append(ref.getString());
    }

    return builder.toString();
  }

  public static abstract class Part extends Expression {

  }

  @Getter @Setter
  public static class LiteralPart extends Part {

    private String value;

    @Override
    public ReturnValue execute(ExecContext context, Scope scope) throws EvaluationError {
      return ReturnValue.directWrap(value);
    }

    @Override
    public <R, C> R visit(NodeVisitor<R, C> visitor, C context) {
      return visitor.visitLiteralPart(this, context);
    }
  }

  @Getter @Setter
  public static class ExpressionPart extends Part {

    private Expression expr;

    @Override
    public ReturnValue execute(ExecContext context, Scope scope) throws EvaluationError {
      return expr.execute(context, scope);
    }

    @Override
    public <R, C> R visit(NodeVisitor<R, C> visitor, C context) {
      return visitor.visitExprPart(this, context);
    }
  }
}
