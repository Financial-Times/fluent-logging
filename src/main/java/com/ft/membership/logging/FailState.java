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
    context.with(Key.OperationState, "fail");

    context.log(Outcome.Failure, Level.ERROR);
    context.clear();
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
