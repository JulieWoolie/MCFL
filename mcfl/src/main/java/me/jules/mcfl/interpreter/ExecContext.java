package me.jules.mcfl.interpreter;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ExecContext {

  private String scriptName;

  private CommandExecutor executor;

  private Scope globalScope;

  private DebugHandler debugHandler;
}
