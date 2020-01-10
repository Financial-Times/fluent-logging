package com.ft.membership.logging;

public class OperationConstructedState implements OperationState {
  private static final OperationConstructedState INSTANCE = new OperationConstructedState();

  protected OperationConstructedState() {}

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

  static OperationConstructedState from(final SimpleOperationContext context) {
    context.addIdentity(context.getName());
    return INSTANCE;
  }
}
