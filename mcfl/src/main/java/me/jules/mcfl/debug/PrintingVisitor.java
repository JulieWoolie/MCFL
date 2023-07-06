package me.jules.mcfl.debug;

import java.io.PrintStream;
import lombok.RequiredArgsConstructor;
import me.jules.mcfl.Location;
import me.jules.mcfl.ast.BinaryExpr;
import me.jules.mcfl.ast.Block;
import me.jules.mcfl.ast.BooleanLiteral;
import me.jules.mcfl.ast.CallExpr;
import me.jules.mcfl.ast.CommandStatement;
import me.jules.mcfl.ast.DebuggerStatement;
import me.jules.mcfl.ast.DoWhileStatement;
import me.jules.mcfl.ast.ExprStatement;
import me.jules.mcfl.ast.ForStatement;
import me.jules.mcfl.ast.FunctionFile;
import me.jules.mcfl.ast.FunctionStatement;
import me.jules.mcfl.ast.Identifier;
import me.jules.mcfl.ast.IfStatement;
import me.jules.mcfl.ast.LoopFlowStatement;
import me.jules.mcfl.ast.Node;
import me.jules.mcfl.ast.NodeVisitor;
import me.jules.mcfl.ast.NullLiteral;
import me.jules.mcfl.ast.NumberLiteral;
import me.jules.mcfl.ast.PropertyAccessExpr;
import me.jules.mcfl.ast.ReturnStatement;
import me.jules.mcfl.ast.StringLiteral;
import me.jules.mcfl.ast.TemplatedString;
import me.jules.mcfl.ast.TemplatedString.ExpressionPart;
import me.jules.mcfl.ast.TemplatedString.LiteralPart;
import me.jules.mcfl.ast.ThrowStatement;
import me.jules.mcfl.ast.UnaryExpr;
import me.jules.mcfl.ast.VariableDefinition;
import me.jules.mcfl.ast.WhileStatement;
import me.jules.mcfl.debug.PrintingVisitor.Printer;

public final class PrintingVisitor implements NodeVisitor<Void, Printer> {

  private static final PrintingVisitor VISITOR = new PrintingVisitor();

  private PrintingVisitor() {
  }

  @RequiredArgsConstructor
  static class Printer {

    private final PrintStream out;

    private int indent;

    private String indentString = "  ";

    Printer incIndent() {
      indent++;
      return this;
    }

    Printer decIndent() {
      indent--;
      return this;
    }

    Printer appendIndent() {
      if (indent < 1) {
        return this;
      }

      out.append(indentString.repeat(indent));
      return this;
    }

    Printer append(Object o) {
      out.append(String.valueOf(o));
      return this;
    }

    Printer append(Location location) {
      return append(location.line())
          .append(':').append(location.column())
          .append(":").append(location.cursor());
    }

    Printer append(char c) {
      out.append(c);
      return this;
    }

    Printer append(int c) {
      out.print(c);
      return this;
    }

    Printer append(boolean c) {
      out.print(c);
      return this;
    }

    Printer nlIndent() {
      return append("\n").appendIndent();
    }
  }

  public static void print(Node node, PrintStream output) {
    Printer printer = new Printer(output);
    node.visit(VISITOR, printer);
    output.print('\n');
  }

  private void blockVisit(Block block, Printer out) {
    out.append("(position=")
        .append(block.getPosition())
        .append(")");

    if (!block.getFunctions().isEmpty()) {
      out.incIndent();

      out.nlIndent();
      out.append("functions(")
          .append(block.getFunctions().size())
          .append("):");

      out.incIndent();

      for (var f: block.getFunctions()) {
        out.nlIndent();
        f.visit(this, out);
      }

      out.decIndent();
      out.decIndent();
    }

    if (!block.getBody().isEmpty()) {
      out.incIndent();

      out.nlIndent();
      out.append("statements(")
          .append(block.getBody().size())
          .append("):");

      out.incIndent();

      for (var f: block.getBody()) {
        out.nlIndent();
        f.visit(this, out);
      }

      out.decIndent();
      out.decIndent();
    }
  }

  @Override
  public Void visitRoot(FunctionFile file, Printer out) {
    out.append("root");
    blockVisit(file, out);
    return null;
  }

  @Override
  public Void visitCommand(CommandStatement expr, Printer out) {
    out.append("command(position=")
        .append(expr.getPosition())
        .append(", parts=");

    visitTemplateString(expr.getString(), out);
    out.append(")");

    return null;
  }

  @Override
  public Void visitIdentifier(Identifier expr, Printer out) {
    out.append("id('")
        .append(expr.getValue())
        .append("'")
        .append(", position=")
        .append(expr.getPosition())
        .append(")");

    return null;
  }

  @Override
  public Void visitVarDefinition(VariableDefinition def, Printer out) {
    out.append("var_def(")
        .append("type=")
        .append(def.getType().name().toLowerCase())
        .append(", name=");

    def.getName().visit(this, out);

    if (def.getValue() != null) {
      out.append(", value=");
      def.getValue().visit(this, out);
    }

    out.append(")");
    return null;
  }

  @Override
  public Void visitBlock(Block block, Printer out) {
    out.append("block");
    blockVisit(block, out);
    return null;
  }

  @Override
  public Void visitNull(NullLiteral expr, Printer out) {
    out.append("null_literal(position=").append(expr.getPosition()).append(")");
    return null;
  }

  @Override
  public Void visitBoolean(BooleanLiteral expr, Printer out) {
    out.append("boolean_literal(")
        .append(expr.value())
        .append(", position=")
        .append(expr.getPosition())
        .append(")");

    return null;
  }

  @Override
  public Void visitString(StringLiteral expr, Printer out) {
    out.append("string_literal(position=")
        .append(expr.getPosition())
        .append(", parts=");

    visitTemplateString(expr.getString(), out);
    out.append(")");

    return null;
  }

  private void visitTemplateString(TemplatedString string, Printer out) {
    var it = string.getParts().iterator();
    out.append("[");

    while (it.hasNext()) {
      var n = it.next();
      n.visit(this, out);

      if (it.hasNext()) {
        out.append(", ");
      }
    }

    out.append("]");
  }

  @Override
  public Void visitNumber(NumberLiteral expr, Printer out) {
    out.append("number_literal(")
        .append(expr.getNumber())
        .append(", position=")
        .append(expr.getPosition())
        .append(")");

    return null;
  }

  @Override
  public Void visitFunction(FunctionStatement statement, Printer out) {
    out.append("function(name=");
    statement.getName().visit(this, out);

    out.append(", parameters=[");

    var it = statement.getParameters().iterator();
    while (it.hasNext()) {
      var n = it.next();
      n.visit(this, out);

      if (it.hasNext()) {
        out.append(", ");
      }
    }
    out.append("]): ");

    statement.getBody().visit(this, out);
    return null;
  }

  @Override
  public Void visitFunctionCall(CallExpr expr, Printer out) {
    out.append("func_call(target=");
    expr.getTarget().visit(this, out);
    out.append(", arguments=[");

    var it = expr.getArguments().iterator();
    while (it.hasNext()) {
      var n = it.next();
      n.visit(this, out);

      if (it.hasNext()) {
        out.append(", ");
      }
    }
    out.append("])");
    return null;
  }

  @Override
  public Void visitExprStatement(ExprStatement statement, Printer out) {
    out.append("expr_statement(");
    statement.getExpr().visit(this, out);
    out.append(")");
    return null;
  }

  @Override
  public Void visitReturn(ReturnStatement statement, Printer out) {
    out.append("return(");

    if (statement.getValue() != null) {
      out.append("value=");
      statement.getValue().visit(this, out);
      out.append(", ");
    }

    out.append("position=")
        .append(statement.getPosition())
        .append(")");

    return null;
  }

  @Override
  public Void visitLoopFlow(LoopFlowStatement statement, Printer out) {
    out.append("loop_flow_control(type=")
        .append(statement.getType().name().toLowerCase());

    if (statement.getLabel() != null) {
      out.append(", label=");
      statement.getLabel().visit(this, out);
    }

    out.append(", position=")
        .append(statement.getPosition())
        .append(")");

    return null;
  }

  @Override
  public Void visitIf(IfStatement s, Printer out) {
    out.append("if(condition=");
    s.getCondition().visit(this, out);
    out.append(", position=")
        .append(s.getPosition())
        .append("): ");

    out.incIndent();
    out.nlIndent();
    out.append("body: ");
    s.getBody().visit(this, out);
    out.decIndent();

    if (s.getElseStatement() != null) {
      out.incIndent();
      out.nlIndent();
      out.append("else: ");
      s.getElseStatement().visit(this, out);
      out.decIndent();
    }

    return null;
  }

  @Override
  public Void visitExprPart(ExpressionPart part, Printer out) {
    out.append("expr_part(");
    part.getExpr().visit(this, out);
    out.append(")");
    return null;
  }

  @Override
  public Void visitLiteralPart(LiteralPart part, Printer out) {
    out.append("literal_part('").append(part.getValue()).append("')");
    return null;
  }

  @Override
  public Void visitDebugger(DebuggerStatement statement, Printer out) {
    out.append("debugger(");

    if (statement.getExpression() != null) {
      out.append("expr=");
      statement.getExpression().visit(this, out);
      out.append(", ");
    }

    out.append("position=")
        .append(statement.getPosition())
        .append(")");

    return null;
  }

  @Override
  public Void visitBinary(BinaryExpr expr, Printer out) {
    out.append("binary_expr(lhs=");
    expr.getLhs().visit(this, out);
    out.append(", rhs=");
    expr.getRhs().visit(this, out);

    out.append(", op=")
        .append(expr.getOp().name().toLowerCase())
        .append(", position=")
        .append(expr.getPosition())
        .append(")");

    return null;
  }

  @Override
  public Void visitUnary(UnaryExpr expr, Printer out) {
    out.append("unary_expr(expr=");
    expr.getExpression().visit(this, out);
    out.append(", operation=")
        .append(expr.getOperation().name().toLowerCase())
        .append(", position=")
        .append(expr.getPosition())
        .append(")");

    return null;
  }

  @Override
  public Void visitThrow(ThrowStatement statement, Printer out) {
    out.append("throw(");

    if (statement.getMessage() != null) {
      out.append("message=");
      statement.getMessage().visit(this, out);
      out.append(", ");
    }

    out.append("position=")
        .append(statement.getPosition())
        .append(")");

    return null;
  }

  @Override
  public Void visitFor(ForStatement statement, Printer out) {
    out.append("for(");

    if (statement.getFirst() != null) {
      out.append("first=");
      statement.getFirst().visit(this, out);
      out.append(", ");
    }

    if (statement.getCondition() != null) {
      out.append("condition=");
      statement.getCondition().visit(this, out);
      out.append(", ");
    }

    if (statement.getThird() != null) {
      out.append("third=");
      statement.getThird().visit(this, out);
      out.append(", ");
    }

    out.append("position=")
        .append(statement.getPosition())
        .append(")");

    return null;
  }

  @Override
  public Void visitPropertyAccess(PropertyAccessExpr expr, Printer out) {
    out.append("property_access_expr(target=");
    expr.getTarget().visit(this, out);
    out.append(", property_name=");
    expr.getPropertyName().visit(this, out);

    out.append(", position=")
        .append(expr.getPosition())
        .append(")");

    return null;
  }

  @Override
  public Void visitWhile(WhileStatement statement, Printer printer) {
    return null;
  }

  @Override
  public Void visitDoWhile(DoWhileStatement statement, Printer printer) {
    return null;
  }
}
