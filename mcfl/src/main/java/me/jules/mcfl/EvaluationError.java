package me.jules.mcfl;

import lombok.Getter;

public class EvaluationError extends Exception {

  @Getter
  private Location scriptLocation;

  public EvaluationError(Location scriptLocation) {
    this.scriptLocation = scriptLocation;
  }

  public EvaluationError(String message, Location scriptLocation) {
    super(message);
    this.scriptLocation = scriptLocation;
  }

  public EvaluationError(String message, Throwable cause, Location scriptLocation) {
    super(message, cause);
    this.scriptLocation = scriptLocation;
  }

  public EvaluationError() {
  }

  public EvaluationError(String message) {
    super(message);
  }

  public EvaluationError(String message, Throwable cause) {
    super(message, cause);
  }

  @Override
  public String getMessage() {
    if (scriptLocation == null) {
      return super.getMessage();
    }

    return "%s at %s".formatted(super.getMessage(), scriptLocation);
  }

  public String getBaseMessage() {
    return super.getMessage();
  }
}
