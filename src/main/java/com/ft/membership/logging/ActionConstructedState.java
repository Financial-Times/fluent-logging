package com.ft.membership.logging;

import java.util.Map;

public class ActionConstructedState implements OperationState {
  private SimpleOperationContext context;
  private final String type = "action";

  ActionConstructedState(SimpleOperationContext simpleOperationContext) {
    context = simpleOperationContext;
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
  public void start() {
    context.setState(new StartedState(context));
  }

  @Override
  public void succeed() {
    context.setState(new SuccessState(context));
  }

  @Override
  public void fail() {
    context.setState(new FailState(context));
  }
}
