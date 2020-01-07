package com.ft.membership.logging;

public interface OperationState {
  String getType();

  void start();

  void succeed();

  void fail();
}
