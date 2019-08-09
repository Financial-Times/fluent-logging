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
  public void with(Map<String, Object> keyValues) {
    context.addParam(keyValues);
  }

  @Override
  public void with(String key, Object value) {
    context.addParam(key, value);
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
