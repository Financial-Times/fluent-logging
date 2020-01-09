package com.ft.membership.logging;

public class OperationConstructedState implements OperationState {

  private static final String type = "operation";
  private static final OperationConstructedState INSTANCE = new OperationConstructedState();

  protected OperationConstructedState() { }

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
    context.setState(new SuccessState(context));
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
