package com.ft.membership.logging;

import static com.ft.membership.logging.Preconditions.checkNotNull;

import java.util.Map;

public class Failure extends Parameters implements LoggingTerminal {

  private Operation operation;
  private Exception thrown;
  private String failureMessage;

  Failure(final Operation operation) {
    this.operation = operation;
  }

  /**
   * add a full exception with stack-trace to the failure.
   *
   * @param e a Throwable
   * @return the Failure
   */
  public Failure throwingException(final Exception e) {
    this.thrown = checkNotNull(e, "require exception");
    if (failureMessage == null) {
      withMessage(e.getMessage());
    }
    return this;
  }

  /**
   * add the message from an exception to the failure, use if you don't want the stack trace.
   *
   * <p>NB overwrites {@link #withMessage(String)}
   *
   * @param t a Throwable
   * @return the Failure
   */
  public Failure withMessage(final Throwable t) {
    this.failureMessage = t.getMessage();
    return this;
  }

  /**
   * add a message field to the failure.
   *
   * <p>NB overwrites {@link #withMessage(Throwable)}
   *
   * @param message a message
   * @return the Failure
   */
  public Failure withMessage(final String message) {
    this.failureMessage = message;
    return this;
  }

  /**
   * add a key-value detail to the failure.
   *
   * @param key a key
   * @param detail a value
   * @return the Failure
   */
  public Failure withDetail(final String key, final Object detail) {
    put(key, detail);
    return this;
  }

  /**
   * add a key-value detail to the failure.
   *
   * @param key a key
   * @param detail a value
   * @return the Failure
   */
  public Failure withDetail(final Key key, final Object detail) {
    put(key, detail);
    return this;
  }

  /**
   * add all key-values from a map as detail of the failure.
   *
   * @param keyValues a map
   * @return the Failure
   */
  public Failure withDetail(final Map<String, Object> keyValues) {
    putAll(keyValues);
    return this;
  }

  /**
   * log this failure with an <tt>ERROR</tt> log-level, using the log context passed when starting
   * the operation.
   */
  public void log() {
    log(operation.getActorOrLogger());
  }

  /**
   * log this failure with an <tt>ERROR</tt> log-level
   *
   * @param actorOrLogger an alternative logger or object for log context
   */
  public void log(Object actorOrLogger) {
    logError(actorOrLogger);
  }

  /**
   * log this failure with a <tt>INFO</tt> log-level, instead of <tt>ERROR</tt>.
   *
   * @param actorOrLogger logger or object for log context
   */
  public void logInfo(Object actorOrLogger) {
    new LogFormatter(actorOrLogger).logInfo(operation, this, true);
  }

  /**
   * log this failure with a <tt>WARN</tt> log-level, instead of <tt>ERROR</tt>.
   *
   * @param actorOrLogger logger or object for log context
   */
  public void logWarn(Object actorOrLogger) {
    new LogFormatter(actorOrLogger).logWarn(operation, this, true);
  }

  private void logError(Object actorOrLogger) {
    new LogFormatter(actorOrLogger).logError(operation, this, true);
  }

  boolean didThrow() {
    return thrown != null;
  }

  Exception getThrown() {
    return thrown;
  }

  String getFailureMessage() {
    return failureMessage;
  }
}
