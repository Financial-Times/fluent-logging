package com.ft.membership.logging;

public class SuccessState implements OperationState {
  private static final SuccessState INSTANCE = new SuccessState();

  private SuccessState() {}

  static SuccessState from(final OperationContext context) {
    context.with(Key.OperationState, "success");
    context.log(Outcome.Success);

    return INSTANCE;
  }
}
