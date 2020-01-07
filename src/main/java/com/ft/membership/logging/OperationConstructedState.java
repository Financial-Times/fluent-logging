package com.ft.membership.logging;

public class OperationConstructedState implements OperationState {

  private SimpleOperationContext context;
  private final String type = "operation";

  OperationConstructedState(final SimpleOperationContext simpleOperationContext) {
    context = simpleOperationContext;
    context.setState(this);

    context.addIdentity(context.getName());
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public void start() {
    context.setState(new StartedState(context));
  }

  @Override
  public void succeed() {
    context.setState(new SuccessState(context));
  }

  @Override
  public void fail() {
    context.setState(new FailState(context));
  }
}
