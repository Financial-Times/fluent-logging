package com.ft.membership.logging;

import org.slf4j.event.Level;

import java.util.Map;

public class FailState implements OperationState {
  private final String type;
  private SimpleOperationContext context;

  public FailState(SimpleOperationContext simpleOperationContext) {
    context = simpleOperationContext;
    type = context.getType();
    context.setState(this);

    context.log(Outcome.Failure, Level.ERROR);
    context.clear();
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
}
