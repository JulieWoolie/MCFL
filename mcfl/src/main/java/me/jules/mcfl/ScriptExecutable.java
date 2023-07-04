package me.jules.mcfl;

import me.jules.mcfl.interpreter.ExecContext;
import me.jules.mcfl.interpreter.ReturnValue;
import me.jules.mcfl.interpreter.Scope;

public interface ScriptExecutable {

  ReturnValue execute(ExecContext context, Scope scope) throws EvaluationError;
}
