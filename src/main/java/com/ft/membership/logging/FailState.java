package com.ft.membership.logging;

import org.slf4j.event.Level;

public class FailState implements OperationState {
  private static final FailState INSTANCE = new FailState();

  private FailState() { }

  @Override
  public void start(final OperationContext context) {}

  @Override
  public void succeed(final OperationContext context) {}

  @Override
  public void fail(final OperationContext context) {}

  public static OperationState of(final OperationContext context) {
    context.with(Key.OperationState, "fail");
    context.log(Outcome.Failure, Level.ERROR);

    return INSTANCE;
  }
}
