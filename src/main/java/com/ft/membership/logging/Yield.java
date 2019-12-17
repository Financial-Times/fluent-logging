package com.ft.membership.logging;

import java.util.Map;

public class Yield extends Parameters implements LoggingTerminal {
  private final Operation operation;

  Yield(final Operation operation) {
    this.operation = operation;
  }

  /**
   * add a key-value to the yield.
   *
   * @param key a key.
   * @param value a value.
   * @return the Yield
   */
  public Yield yielding(final String key, final Object value) {
    put(key, value);
    return this;
  }

  /**
   * add a key-value to the yield.
   *
   * @param key a key.
   * @param value a value.
   * @return the Yield
   */
  public Yield yielding(final Key key, final Object value) {
    put(key, value);
    return this;
  }

  /**
   * add all key-values from a map to the yield.
   *
   * @param keyValues a map of key-values.
   * @return the Yield
   */
  public Yield yielding(final Map<String, Object> keyValues) {
    putAll(keyValues);
    return this;
  }

  /**
   * log this success with an <tt>INFO</tt> log-level, using the log context passed when starting
   * the operation.
   */
  public void log() {
    log(operation.getActorOrLogger());
  }

  /**
   * log this success with an <tt>INFO</tt> log-level
   *
   * @param actorOrLogger an alternative logger or object for log context
   */
  public void log(final Object actorOrLogger) {
    logInfo(actorOrLogger);
  }

  /**
   * log this success with a <tt>DEBUG</tt> log-level, instead of <tt>INFO</tt>.
   *
   * @param actorOrLogger logger or object for log context
   */
  public void logDebug(Object actorOrLogger) {
    new LogFormatter(actorOrLogger).logDebug(operation, this, true);
  }

  private void logInfo(Object actorOrLogger) {
    new LogFormatter(actorOrLogger).logInfo(operation, this, true);
  }
}
