package com.ft.membership.logging;

public class ActionConstructedState implements OperationState {
  private static final ActionConstructedState INSTANCE = new ActionConstructedState();

  private ActionConstructedState() {}

  @Override
  public void start(final OperationContext context) {
    changeState(context, StartedState.from(context));
  }

  @Override
  public void succeed(final OperationContext context) {
    changeState(context, SuccessState.from(context));
  }

  @Override
  public void fail(final OperationContext context) {
    changeState(context, FailState.from(context));
  }

  public static ActionConstructedState from(final SimpleOperationContext context) {
   return INSTANCE;
  }

  @Override
  public String toString() {
    return "actionConstructedState";
  }
}
