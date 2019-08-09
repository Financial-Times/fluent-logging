package com.ft.membership.logging;

public enum Key {
  UserId("userId"),
  UserEmail("email"),
  Result("result"),
  DebugMessage("debugMessage"),
  Operation("operation");

  private final String key;

  Key(String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }
}
