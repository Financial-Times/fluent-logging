package com.ft.membership.logging;

public enum Outcome {
  Success("success"),
  Failure("failure");

  private final String key;

  Outcome(String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }
}
