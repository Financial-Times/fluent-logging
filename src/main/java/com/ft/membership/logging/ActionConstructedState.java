package com.ft.membership.logging;

public class ActionConstructedState implements OperationState {
  private static final String type = "action";
  private static final ActionConstructedState INSTANCE = new ActionConstructedState();

  private ActionConstructedState() {}

  @Override
  public String getType() {
    return type;
  }

  @Override
  public void start(final OperationContext context) {
    context.setState(StartedState.of(context));
  }

  @Override
  public void succeed(final OperationContext context) {
    context.setState(SuccessState.of(context));
  }

  @Override
  public void fail(final OperationContext context) {
    context.setState(FailState.of(context));
  }

  static ActionConstructedState from(final SimpleOperationContext context) {
   return INSTANCE;
  }
}
