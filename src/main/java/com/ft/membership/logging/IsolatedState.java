package com.ft.membership.logging;


import java.util.Map;

public class IsolatedState implements OperationState {
  private String type;
  private SimpleOperationContext context;

  // Use the static factory method
  private IsolatedState() {}
  private IsolatedState(SimpleOperationContext simpleOperationContext, String type) {
    context = simpleOperationContext;
    this.type = type;
    context.setState(this);
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public void start() {}

  @Override
  public void succeed() {}

  @Override
  public void fail() {}

  public static IsolatedState from(SimpleOperationContext context, String type) {
    return new IsolatedState(context, type);
  }
}
