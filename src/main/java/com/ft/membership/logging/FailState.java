package com.ft.membership.logging;

import org.slf4j.event.Level;

import java.util.Map;

public class FailState implements OperationState {
  private final String type;

  public FailState(OperationContext context) {
    type = context.getType();
    context.with(Key.OperationState, "fail");
    context.log(Outcome.Failure, Level.ERROR);
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public void start(final OperationContext context) {}

  @Override
  public void succeed(final OperationContext context) {}

  @Override
  public void fail(final OperationContext context) {}

  public static OperationState of(final OperationContext context) {
    return new FailState(context);
  }
}
