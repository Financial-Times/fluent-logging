package com.ft.membership.logging;

public enum Key {
  Result("result"),
  ExceptionType("exceptionType"),
  ErrorMessage("errorMessage"),
  DebugMessage("debugMessage"),
  LoggerState("loggerState"),
  Operation("operation"),
  Action("action");

  private final String key;

  Key(String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }
}
