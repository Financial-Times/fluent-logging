package com.ft.membership.logging;

public class OperationConstructedState implements OperationState {
  private static final OperationConstructedState INSTANCE = new OperationConstructedState();

  protected OperationConstructedState() {}

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

  static OperationConstructedState from(final SimpleOperationContext context) {
    context.addIdentity(context.getName());
    return INSTANCE;
  }
}
