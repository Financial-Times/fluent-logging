package com.ft.membership.logging;

import org.slf4j.event.Level;

import java.util.Map;

public class SuccessState implements OperationState {
  private final SimpleOperationContext context;
  private final String type;

  SuccessState(SimpleOperationContext simpleOperationContext) {
    context = simpleOperationContext;
    type = context.getType();
    context.with(Key.OperationState, "success");
    context.log(Outcome.Success, Level.INFO);
  }

  @Override
  public String getType() {return type;}

  @Override
  public void start() {}

  @Override
  public void succeed() {}

  @Override
  public void fail() {}
}
