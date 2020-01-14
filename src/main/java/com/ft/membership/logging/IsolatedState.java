package com.ft.membership.logging;


public class IsolatedState implements OperationState {
  private static IsolatedState INSTANCE = new IsolatedState();

  // Use the static factory method
  private IsolatedState() {}

  public static IsolatedState from(SimpleOperationContext context) {
    return INSTANCE;
  }

  @Override
  public String toString() {
    return "isolatedState";
  }
}
