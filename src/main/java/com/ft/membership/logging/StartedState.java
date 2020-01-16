package com.ft.membership.logging;

public class StartedState implements LoggerState {
  private static final StartedState INSTANCE = new StartedState();

  private StartedState() {}

  @Override
  public void succeed(FluentLogger context) {
    changeState(context, SuccessState.from(context));
  }

  @Override
  public void fail(FluentLogger context) {
    changeState(context, FailState.from(context));
  }

  public static StartedState from(final FluentLogger context) {
    context.with(Key.LoggerState, "started");
    context.log();

    return INSTANCE;
  }

  @Override
  public String toString() {
    return "startedState";
  }
}
