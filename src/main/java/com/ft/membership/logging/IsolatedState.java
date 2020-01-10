package com.ft.membership.logging;


public class IsolatedState implements OperationState {
  private static IsolatedState INSTANCE = new IsolatedState();

  // Use the static factory method
  private IsolatedState() {}

  @Override
  public void start(OperationContext context) {}

  @Override
  public void succeed(OperationContext context) {}

  @Override
  public void fail(OperationContext context) {}

  public static IsolatedState of(SimpleOperationContext context, String type) {
    return INSTANCE;
  }
}
