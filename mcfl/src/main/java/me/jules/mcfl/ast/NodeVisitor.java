package me.jules.mcfl.ast;

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
}