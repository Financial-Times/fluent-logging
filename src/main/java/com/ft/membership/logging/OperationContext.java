package com.ft.membership.logging;

import java.util.Collections;
import java.util.Map;
import org.slf4j.event.Level;

public abstract class OperationContext implements AutoCloseable {

  protected Parameters parameters;
  protected String name;
  protected Object actorOrLogger;
  protected OperationState state;

  protected abstract void clear();

  public abstract void logDebug(final String debugMessage, final Map<String, Object> keyValues);


  public OperationContext with(final Key key, final Object value) {
    return with(key.getKey(), value);
  }

  public OperationContext with(final String key, final Object value) {
    state.with(key, value);
    return this;
  }

  public OperationContext with(final Map<String, Object> keyValues) {
    state.with(keyValues);
    return this;
  }

  public OperationContext started() {
    state.start();
    return this;
  }

  public void wasSuccessful() {
    state.succeed();
  }

  public void wasSuccessful(final Object result) {
    with(Key.Result, result);
    this.state.succeed();
  }

  public void wasSuccessful(final Object result, final Level level) {
    // TODO decide if we want to support different levels of result logs
  }

  public void wasFailure() {
    state.fail();
  }

  public void wasFailure(final Object result) {
    with(Key.Result, result);
    state.fail();
  }

  public void wasFailure(final Object result, final Level level) {
    // TODO decide if we want to support different levels of result logs
  }

  public void logDebug(final String debugMessage) {
    logDebug(debugMessage, Collections.emptyMap());
  }


  public void log(Level level) {
    log(null, level);
  }

  void log(final Outcome outcome, final Level logLevel) {
    new LogFormatter(actorOrLogger).log(this, outcome, logLevel);
  }

  String getName() {
    return name;
  }

  Map<String, Object> getParameters() {
    return parameters.getParameters();
  }

  Object getActorOrLogger() {
    return actorOrLogger;
  }

  String getType() {
    return this.state.getType();
  }

  void setState(OperationState operationState) {
    this.state = operationState;
  };

  void addParam(final String key, final Object value) {
    parameters.put(key, value);
  }

  void addParam(final Map<String, Object> keyValues) {
    parameters.putAll(keyValues);
  }

  @Override
  public void close() {
    if (!(state instanceof FailState || state instanceof StartedState)) {
      wasFailure("\"Programmer error: operation auto-closed before wasSuccessful() or wasFailure() called.\"");
    }
  }
}
