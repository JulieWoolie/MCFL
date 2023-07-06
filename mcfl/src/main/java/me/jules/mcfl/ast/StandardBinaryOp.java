package me.jules.mcfl.ast;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.jules.mcfl.EvaluationError;
import me.jules.mcfl.TokenType;
import me.jules.mcfl.interpreter.ValueRef;

@Getter @RequiredArgsConstructor
public enum StandardBinaryOp implements BinaryOp {

  EQUALS (TokenType.EQUALS, null) {
    @Override
    public ValueRef compute(ValueRef lhs, ValueRef rhs) throws EvaluationError {
      return ValueRef.direct(lhs.operatorEquals(rhs));
    }
  },

  NEGATED_EQUALS (TokenType.N_EQUALS, null) {
    @Override
    public ValueRef compute(ValueRef lhs, ValueRef rhs) throws EvaluationError {
      return ValueRef.direct(!lhs.operatorEquals(rhs));
    }
  },

  ADD (TokenType.ADD, TokenType.ASSIGN_ADD) {
    @Override
    public ValueRef compute(ValueRef lhs, ValueRef rhs) throws EvaluationError {
      return lhs.operatorAdd(rhs);
    }
  },

  SUB (TokenType.SUB, TokenType.ASSIGN_SUB) {
    @Override
    public ValueRef compute(ValueRef lhs, ValueRef rhs) throws EvaluationError {
      return lhs.operatorSub(rhs);
    }
  },

  MUL (TokenType.MUL, TokenType.ASSIGN_MUL) {
    @Override
    public ValueRef compute(ValueRef lhs, ValueRef rhs) throws EvaluationError {
      return lhs.operatorMul(rhs);
    }
  },

  DIV (TokenType.DIV, TokenType.ASSIGN_DIV) {
    @Override
    public ValueRef compute(ValueRef lhs, ValueRef rhs) throws EvaluationError {
      return lhs.operatorDiv(rhs);
    }
  },

  POW (TokenType.POW, TokenType.ASSIGN_POW) {
    @Override
    public ValueRef compute(ValueRef lhs, ValueRef rhs) throws EvaluationError {
      return lhs.operatorPow(rhs);
    }
  },

  MOD (TokenType.MOD, TokenType.ASSIGN_MOD) {
    @Override
    public ValueRef compute(ValueRef lhs, ValueRef rhs) throws EvaluationError {
      return lhs.operatorModulo(rhs);
    }
  },

  OR (TokenType.OR, TokenType.ASSIGN_OR) {
    @Override
    public ValueRef compute(ValueRef lhs, ValueRef rhs) throws EvaluationError {
      return lhs.operatorOr(rhs);
    }
  },

  XOR (TokenType.XOR, TokenType.ASSIGN_XOR) {
    @Override
    public ValueRef compute(ValueRef lhs, ValueRef rhs) throws EvaluationError {
      return lhs.operatorXor(rhs);
    }
  },

  AND (TokenType.AND, TokenType.ASSIGN_AND) {
    @Override
    public ValueRef compute(ValueRef lhs, ValueRef rhs) throws EvaluationError {
      return lhs.operatorAnd(rhs);
    }
  },

  L_SHIFT (TokenType.SHIFT_LEFT, TokenType.ASSIGN_SHIFT_LEFT) {
    @Override
    public ValueRef compute(ValueRef lhs, ValueRef rhs) throws EvaluationError {
      return lhs.operatorLeftShift(rhs);
    }
  },

  R_SHIFT (TokenType.SHIFT_RIGHT, TokenType.ASSIGN_SHIFT_RIGHT) {
    @Override
    public ValueRef compute(ValueRef lhs, ValueRef rhs) throws EvaluationError {
      return lhs.operatorRightShift(rhs);
    }
  },

  U_L_SHIFT (TokenType.USHIFT_LEFT, TokenType.ASSIGN_USHIFT_LEFT) {
    @Override
    public ValueRef compute(ValueRef lhs, ValueRef rhs) throws EvaluationError {
      return lhs.operatorUnsignedLeftShift(rhs);
    }
  },

  U_R_SHIFT (TokenType.USHIFT_RIGHT, TokenType.ASSIGN_USHIFT_RIGHT) {
    @Override
    public ValueRef compute(ValueRef lhs, ValueRef rhs) throws EvaluationError {
      return lhs.operatorUnsignedRightShift(rhs);
    }
  },

  LESS_THAN (TokenType.LT, null) {
    @Override
    public ValueRef compute(ValueRef lhs, ValueRef rhs) throws EvaluationError {
      int comparison = lhs.operatorCompare(rhs);

      if (comparison < 0) {
        return ValueRef.TRUE;
      } else {
        return ValueRef.FALSE;
      }
    }
  },

  LESS_THAN_EQUAL (TokenType.LTE, null) {
    @Override
    public ValueRef compute(ValueRef lhs, ValueRef rhs) throws EvaluationError {
      int comparison = lhs.operatorCompare(rhs);

      if (comparison <= 0) {
        return ValueRef.TRUE;
      } else {
        return ValueRef.FALSE;
      }
    }
  },

  GREATER_THAN (TokenType.GT, null) {
    @Override
    public ValueRef compute(ValueRef lhs, ValueRef rhs) throws EvaluationError {
      int comparison = lhs.operatorCompare(rhs);

      if (comparison > 0) {
        return ValueRef.TRUE;
      } else {
        return ValueRef.FALSE;
      }
    }
  },

  GREATER_THAN_EQUAL (TokenType.GTE, null) {
    @Override
    public ValueRef compute(ValueRef lhs, ValueRef rhs) throws EvaluationError {
      int comparison = lhs.operatorCompare(rhs);

      if (comparison >= 0) {
        return ValueRef.TRUE;
      } else {
        return ValueRef.FALSE;
      }
    }
  },

  ASSIGN (TokenType.ASSIGN, null) {
    @Override
    public ValueRef compute(ValueRef lhs, ValueRef rhs) throws EvaluationError {
      lhs.set(rhs.value());
      return lhs;
    }
  };

  public static final Set<TokenType> BINARY_OP_TOKENS;
  public static final Map<TokenType, BinaryOp> TOKEN_TO_OP;

  static {
    StandardBinaryOp[] ops = values();

    Set<TokenType> types = EnumSet.noneOf(TokenType.class);
    Map<TokenType, BinaryOp> lookup = new HashMap<>();

    for (var o: ops) {
      types.add(o.getTokenType());
      lookup.put(o.getTokenType(), o);

      if (o.getAssignmentToken() == null) {
        continue;
      }

      TokenType t = o.getAssignmentToken();
      BinaryOp op = BinaryOp.assign(o);

      types.add(t);
      lookup.put(t, op);
    }

    BINARY_OP_TOKENS = Collections.unmodifiableSet(types);
    TOKEN_TO_OP = Collections.unmodifiableMap(lookup);
  }

  private final TokenType tokenType;
  private final TokenType assignmentToken;
}
