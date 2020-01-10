package com.ft.membership.logging;

import org.slf4j.event.Level;

public class StartedState implements OperationState {
  private static final StartedState INSTANCE = new StartedState();

  private StartedState() { }

  @Override
  public void succeed(OperationContext context) {
    changeState(context, SuccessState.from(context));
  }

  @Override
  public void fail(OperationContext context) {
    changeState(context, FailState.from(context));
  }

  static StartedState from(final OperationContext context) {
    context.with(Key.OperationState, "started");
    context.log();

    return INSTANCE;
  }
}
