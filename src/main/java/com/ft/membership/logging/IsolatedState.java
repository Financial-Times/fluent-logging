package com.ft.membership.logging;

public class IsolatedState implements LoggerState {
  private static IsolatedState INSTANCE = new IsolatedState();

  private IsolatedState() {}

  public static IsolatedState from(SimpleFluentLogger context) {
    return INSTANCE;
  }

  @Override
  public String toString() {
    return "isolatedState";
  }
}
