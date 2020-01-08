package com.ft.membership.logging;

import java.util.Collections;
import java.util.Map;
import org.slf4j.event.Level;

/**
 * {@code OperationContext} is the intended type to be interacted with when creating Operations
 * Either use the factory methods of {@code SimpleOperationContext} or
 * create your own implementation of this class that fits your needs.
 * You can define new operation states by implementing {@code OperationState}.
 * {@code OperationContext} objects are intended to be used with try-with-resources.
 * When operation is closed it is assumed that they need to be either in {@code SuccessState} or
 * {@code FailState}, if that is not the case with your use case overwrite the close method
 */
public abstract class OperationContext implements AutoCloseable {

  protected Parameters parameters;
  protected String name;
  protected Object actorOrLogger;
  protected OperationState state;

  /**
   * Method used to clear the context.
   * Take care of any side-effects that we have introduced during the operation.
   */
  protected abstract void clear();

  public void logDebug(final String debugMessage) {
    logDebug(debugMessage, Collections.emptyMap());
  }

  public abstract void logDebug(final String debugMessage, final Map<String, Object> keyValues);


  public OperationContext with(final Key key, final Object value) {
    return with(key.getKey(), value);
  }

  public OperationContext with(final String key, final Object value) {
    addParam(key, value);
    return this;
  }

  public OperationContext with(final Map<String, Object> keyValues) {
    addParam(keyValues);
    return this;
  }

  public OperationContext started() {
    state.start();
    return this;
  }

  public void wasSuccessful() {
    state.succeed();
    clear();
  }

  public void wasSuccessful(final Object result) {
    with(Key.Result, result);
    state.succeed();
    clear();
  }

  public void wasSuccessful(final Object result, final Level level) {
    // TODO decide if we want to support different levels of result logs
  }

  public void wasFailure() {
    state.fail();
    clear();
  }

  public void wasFailure(final Object result) {
    with(Key.Result, result);
    state.fail();
    clear();
  }

  public void wasFailure(final Object result, final Level level) {
    // TODO decide if we want to support different levels of result logs
  }


  public void log(Level level) {
    log(null, level);
  }

  @Override
  public void close() {
    if (!(state instanceof FailState || state instanceof SuccessState)) {
      wasFailure("Programmer error: operation auto-closed before wasSuccessful() or wasFailure() called.");
    }

    clear();

    // We need to clear the reference
    state = null;
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

}
