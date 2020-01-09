package com.ft.membership.logging;


public class IsolatedState implements OperationState {
  private String type;

  // Use the static factory method
  private IsolatedState() {}
  private IsolatedState(SimpleOperationContext context, String type) {
    this.type = type;
  }



  @Override
  public String getType() {
    return type;
  }

  @Override
  public void start(OperationContext context) {}

  @Override
  public void succeed(OperationContext context) {}

  @Override
  public void fail(OperationContext context) {}

  public static IsolatedState of(SimpleOperationContext context, String type) {
    return new IsolatedState(context, type);
  }
}
