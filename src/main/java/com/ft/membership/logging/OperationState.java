package com.ft.membership.logging;

/**
 * {@code OperationState} is the basic interface for operation states
 * All operation states need some "type" representing it.
 * {@code OperationState} defines the following state methods (a.k.a operation state transitions):
 * - start
 * - succeed
 * - fail
 * After operation is started it is expected to be finished - either by succeeding or failing.
 * Extend this interface in case you need more operation transitions.
 * {@code OperationState} is intended to be used within {@code OperationContext}.
 */
public interface OperationState {
  String getType();

  void start();

  void succeed();

  void fail();
}
