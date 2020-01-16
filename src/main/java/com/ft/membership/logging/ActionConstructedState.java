package com.ft.membership.logging;

public class ActionConstructedState implements LoggerState {
  private static final ActionConstructedState INSTANCE = new ActionConstructedState();

  private ActionConstructedState() {}

  @Override
  public void start(final FluentLogger context) {
    changeState(context, StartedState.from(context));
  }

  @Override
  public void succeed(final FluentLogger context) {
    changeState(context, SuccessState.from(context));
  }

  @Override
  public void fail(final FluentLogger context) {
    changeState(context, FailState.from(context));
  }

  public static ActionConstructedState from(final SimpleFluentLogger context) {
    return INSTANCE;
  }

  @Override
  public String toString() {
    return "actionConstructedState";
  }
}
