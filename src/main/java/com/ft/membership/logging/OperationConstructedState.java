package com.ft.membership.logging;

public class OperationConstructedState implements LoggerState {
  private static final OperationConstructedState INSTANCE = new OperationConstructedState();

  protected OperationConstructedState() {}

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

  public static OperationConstructedState from(final SimpleFluentLogger context) {
    context.addIdentity(context.getName());
    return INSTANCE;
  }

  @Override
  public String toString() {
    return "operationConstructedState";
  }
}
