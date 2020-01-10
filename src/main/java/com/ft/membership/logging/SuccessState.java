package com.ft.membership.logging;

import org.slf4j.event.Level;

import java.util.Map;

public class SuccessState implements OperationState {
  private static final SuccessState INSTANCE = new SuccessState();

  private SuccessState() {}

  @Override
  public void start(final OperationContext context) {}

  @Override
  public void succeed(final OperationContext context) {}

  @Override
  public void fail(final OperationContext context) {}

  static SuccessState of(final OperationContext context) {
    context.with(Key.OperationState, "success");
    context.log(Outcome.Success, Level.INFO);

    return INSTANCE;
  }
}
