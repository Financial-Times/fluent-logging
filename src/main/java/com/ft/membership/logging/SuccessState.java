package com.ft.membership.logging;

public class SuccessState implements OperationState {
  private static final SuccessState INSTANCE = new SuccessState();

  private SuccessState() {}

  public static SuccessState from(final OperationContext context) {
    context.with(Key.OperationState, "success");
    context.log(Outcome.Success);

    return INSTANCE;
  }

  @Override
  public String toString() {
    return "successState";
  }
}
