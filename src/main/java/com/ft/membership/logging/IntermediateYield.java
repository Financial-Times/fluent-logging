package com.ft.membership.logging;

import java.util.Map;

/** Use an FluentLogger implementation (e.g. SimpleFluentLogger's operation/action) */
@Deprecated
public class IntermediateYield extends Yield {
  private Operation operation;

  IntermediateYield(Operation operation) {
    super(operation);
    this.operation = operation;
  }

  /**
   * add a key-value to the yield.
   *
   * @param key a key.
   * @param value a value.
   * @return the IntermediateYield
   */
  @Override
  public IntermediateYield yielding(final Key key, final Object value) {
    put(key, value);
    return this;
  }

  /**
   * add all key-values from a map to the yield.
   *
   * @param keyValues a map of key-values.
   * @return the IntermediateYield
   */
  @Override
  public IntermediateYield yielding(final Map<String, Object> keyValues) {
    putAll(keyValues);
    return this;
  }

  /**
   * add a key-value to the yield.
   *
   * @param key a key.
   * @param value a value.
   * @return the IntermediateYield
   */
  @Override
  public IntermediateYield yielding(final String key, final Object value) {
    put(key, value);
    return this;
  }

  /**
   * log this success with an <tt>INFO</tt> log-level, using the log context passed when starting
   * the operation.
   */
  @Override
  public void log() {
    new LogFormatter(operation.getActorOrLogger()).logInfo(operation, this, false);
  }

  /**
   * log this success with an <tt>INFO</tt> log-level
   *
   * @param actorOrLogger an alternative logger or object for log context
   */
  @Override
  public void log(final Object actorOrLogger) {
    new LogFormatter(actorOrLogger).logInfo(operation, this, false);
  }

  public void logInfo() {
    new LogFormatter(operation.getActorOrLogger()).logInfo(operation, this, false);
  }

  public void logDebug() {
    new LogFormatter(operation.getActorOrLogger()).logDebug(operation, this, false);
  }

  public void logWarn() {
    new LogFormatter(operation.getActorOrLogger()).logWarn(operation, this, false);
  }

  public void logError() {
    new LogFormatter(operation.getActorOrLogger()).logError(operation, this, false);
  }

  public void logInfo(final Object actorOrLogger) {
    new LogFormatter(actorOrLogger).logInfo(operation, this, false);
  }

  @Override
  public void logDebug(final Object actorOrLogger) {
    new LogFormatter(actorOrLogger).logDebug(operation, this, false);
  }

  public void logWarn(final Object actorOrLogger) {
    new LogFormatter(actorOrLogger).logWarn(operation, this, false);
  }

  public void logError(final Object actorOrLogger) {
    new LogFormatter(actorOrLogger).logError(operation, this, false);
  }
}
