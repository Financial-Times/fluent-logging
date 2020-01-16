package com.ft.membership.logging;

public class SuccessState implements LoggerState {
  private static final SuccessState INSTANCE = new SuccessState();

  private SuccessState() {}

  public static SuccessState from(final FluentLogger context) {
    context.with(Key.LoggerState, "success");
    context.log(Outcome.Success);

    return INSTANCE;
  }

  @Override
  public String toString() {
    return "successState";
  }
}
