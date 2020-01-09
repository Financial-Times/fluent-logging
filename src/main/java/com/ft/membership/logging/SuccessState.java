package com.ft.membership.logging;

import org.slf4j.event.Level;

import java.util.Map;

public class SuccessState implements OperationState {
  private final String type;

  protected SuccessState(OperationContext context) {
    type = context.getType();
    context.with(Key.OperationState, "success");
    context.log(Outcome.Success, Level.INFO);
  }

  @Override
  public String getType() {return type;}

  @Override
  public void start(final OperationContext context) {}

  @Override
  public void succeed(final OperationContext context) {}

  @Override
  public void fail(final OperationContext context) {}

  static SuccessState of(final OperationContext context) {
    return new SuccessState(context);
  }
}
