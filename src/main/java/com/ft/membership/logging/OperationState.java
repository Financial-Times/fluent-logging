package com.ft.membership.logging;

/**
 * {@code OperationState} is the basic interface for operation states {@code OperationState} defines
 * the following state methods (a.k.a operation state transitions): - start - succeed - fail After
 * operation is started it is expected to be finished - either by succeeding or failing. Extend this
 * interface in case you need more operation transitions. {@code OperationState} is intended to be
 * used within {@code FluentLogger}.
 */
public interface OperationState {

  default void start(FluentLogger fluentLogger) {}

  default void succeed(FluentLogger fluentLogger) {}

  default void fail(FluentLogger fluentLogger) {}

  default void changeState(FluentLogger fluentLogger, OperationState operationState) {
    fluentLogger.changeState(operationState);
  }
}
