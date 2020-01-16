package com.ft.membership.logging;

public enum Key {
  UserId("userId"),
  UserEmail("email"),
  Result("result"),
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
