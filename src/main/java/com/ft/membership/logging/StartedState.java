package com.ft.membership.logging;

import org.slf4j.event.Level;

import java.util.Map;
import sun.jvm.hotspot.oops.Instance;

public class StartedState implements OperationState {
  private static final StartedState INSTANCE = new StartedState();

  private StartedState() { }

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
    context.with(Key.OperationState, "started");
    context.log(Level.INFO);

    return INSTANCE;
  }
}
