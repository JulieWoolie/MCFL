package me.jules.mcfl;

import me.jules.mcfl.interpreter.ExecContext;
import me.jules.mcfl.interpreter.ReturnValue;
import me.jules.mcfl.interpreter.Scope;
import me.jules.mcfl.interpreter.ValueRef;

public interface ScriptCallable {

  ReturnValue call(ExecContext ctx, Scope scope, ValueRef[] params) throws EvaluationError;
}
