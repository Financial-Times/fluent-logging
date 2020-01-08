package com.ft.membership.logging;

public class ActionConstructedState implements OperationState {
  private SimpleOperationContext context;
  private final String type = "action";

  ActionConstructedState(SimpleOperationContext simpleOperationContext) {
    context = simpleOperationContext;
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
