package me.jules.mcfl.ast;

import me.jules.mcfl.ast.TemplatedString.ExpressionPart;
import me.jules.mcfl.ast.TemplatedString.LiteralPart;

public interface NodeVisitor<R, C> {

  R visitRoot(FunctionFile file, C c);

  R visitCommand(CommandStatement expr, C c);

  R visitIdentifier(Identifier expr, C c);

  R visitVarDefinition(VariableDefinition def, C c);

  R visitBlock(Block block, C c);

  R visitNull(NullLiteral expr, C c);

  R visitBoolean(BooleanLiteral expr, C c);

  R visitString(StringLiteral expr, C c);

  R visitNumber(NumberLiteral expr, C c);

  R visitFunction(FunctionStatement statement, C c);

  R visitFunctionCall(CallExpr expr, C c);

  R visitExprStatement(ExprStatement statement, C c);

  R visitReturn(ReturnStatement statement, C c);

  R visitLoopFlow(LoopFlowStatement statement, C c);

  R visitIf(IfStatement statement, C c);

  R visitExprPart(ExpressionPart part, C c);

  R visitLiteralPart(LiteralPart part, C c);

  R visitDebugger(DebuggerStatement statement, C c);

  R visitBinary(BinaryExpr expr, C c);

  R visitUnary(UnaryExpr expr, C c);

  R visitThrow(ThrowStatement statement, C c);

  R visitFor(ForStatement statement, C c);

  R visitPropertyAccess(PropertyAccessExpr expr, C c);

  R visitWhile(WhileStatement statement, C c);

  R visitDoWhile(DoWhileStatement statement, C c);
}