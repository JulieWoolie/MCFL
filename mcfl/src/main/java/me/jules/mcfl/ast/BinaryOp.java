package me.jules.mcfl.ast;

import me.jules.mcfl.EvaluationError;
import me.jules.mcfl.interpreter.ValueRef;

public interface BinaryOp {

  ValueRef compute(ValueRef lhs, ValueRef rhs) throws EvaluationError;

  static BinaryOp assign(BinaryOp op) {
    return new AssignmentOp(op);
  }

  String name();

  class AssignmentOp implements BinaryOp {

    private final BinaryOp base;

    public AssignmentOp(BinaryOp base) {
      this.base = base;
    }

    @Override
    public ValueRef compute(ValueRef lhs, ValueRef rhs) throws EvaluationError {
      ValueRef result = base.compute(lhs, rhs);
      lhs.set(result.value());
      return lhs;
    }

    @Override
    public String name() {
      return "assign_" + base.name();
    }
  }
}
