package me.jules.mcfl.ast;

import me.jules.mcfl.EvaluationError;
import me.jules.mcfl.interpreter.ValueRef;

public enum UnaryOp {

  PRE_INC {
    @Override
    public ValueRef compute(ValueRef ref) throws EvaluationError {
      return ref.operatorPreIncrement();
    }
  },

  PRE_DEC {
    @Override
    public ValueRef compute(ValueRef ref) throws EvaluationError {
      return ref.operatorPreDecrement();
    }
  },

  POST_INC {
    @Override
    public ValueRef compute(ValueRef ref) throws EvaluationError {
      return ref.operatorPostIncrement();
    }
  },

  POST_DEC {
    @Override
    public ValueRef compute(ValueRef ref) throws EvaluationError {
      return ref.operatorPostDecrement();
    }
  },

  POSITIVE {
    @Override
    public ValueRef compute(ValueRef ref) throws EvaluationError {
      return ref.operatorPositive();
    }
  },

  NEGATIVE {
    @Override
    public ValueRef compute(ValueRef ref) throws EvaluationError {
      return ref.operatorNegative();
    }
  },

  NEGATE {
    @Override
    public ValueRef compute(ValueRef ref) throws EvaluationError {
      return ref.operatorNegate();
    }
  },

  INVERT {
    @Override
    public ValueRef compute(ValueRef ref) throws EvaluationError {
      return ref.operatorInvert();
    }
  }
  ;

  public abstract ValueRef compute(ValueRef ref) throws EvaluationError;
}
