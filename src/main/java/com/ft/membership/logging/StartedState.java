package com.ft.membership.logging;

import org.slf4j.event.Level;

import java.util.Map;

public class StartedState implements OperationState {
  private String type;
  private SimpleOperationContext context;

  StartedState(SimpleOperationContext simpleOperationContext) {
    context = simpleOperationContext;
    type = context.getType();
    context.setState(this);

    context.log(Level.INFO);
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
    // already started
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
