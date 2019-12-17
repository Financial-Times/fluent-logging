package com.ft.membership.logging;

public enum Key {
  UserId("userId"),
  UserEmail("email");

  private final String key;

  Key(String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }
}
