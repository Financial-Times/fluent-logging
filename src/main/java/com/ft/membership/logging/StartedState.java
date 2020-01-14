package com.ft.membership.logging;

public class StartedState implements OperationState {
  private static final StartedState INSTANCE = new StartedState();

  private StartedState() {}

  @Override
  public void succeed(OperationContext context) {
    changeState(context, SuccessState.from(context));
  }

  @Override
  public void fail(OperationContext context) {
    changeState(context, FailState.from(context));
  }

  public static StartedState from(final OperationContext context) {
    context.with(Key.OperationState, "started");
    context.log();

    return INSTANCE;
  }

  @Override
  public String toString() {
    return "startedState";
  }
}
