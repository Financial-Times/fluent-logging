package com.ft.membership.logging;

import org.slf4j.event.Level;

import java.util.Map;

public class StartedState implements OperationState {
  private String type;

  protected StartedState(OperationContext context) {
    type = context.getType();
    context.with(Key.OperationState, "started");
    context.log(Level.INFO);
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public void start(OperationContext context) {
    // already started
  }

  @Override
  public void succeed(OperationContext context) {
    context.setState(SuccessState.of(context));
  }

  @Override
  public void fail(OperationContext context) {
    context.setState(FailState.of(context));
  }

  static StartedState of(final OperationContext context) {
    return new StartedState(context);
  }
}
