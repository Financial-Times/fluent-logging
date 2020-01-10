package com.ft.membership.logging;

import static com.ft.membership.logging.Preconditions.checkNotNull;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
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
  /**
   * Gives ability to set default layout of the output. One option is key=value, while the other
   * is {"key":"value"}
   * The layout is valid for all following operations.
   * It could be overwritten for specific operation.
  */
  private static Layout defaultLayout = Layout.KeyValuePair;

  /**
   * Gives ability to set default validation pattern for keys/fields.
   * Just as defaultLayout it could be overwritten.
   * One option currently exists - KeyRegex.CamelCase.
   * Could be set or disabled globally or per operation.
   */
  private static Pattern defaultKeyRegexPattern;

  protected Layout layout = defaultLayout;
  protected Pattern keyRegexPattern = defaultKeyRegexPattern;

  protected String name;
  protected Parameters parameters;
  protected Object actorOrLogger;
  protected OperationState state;

  /**
   * Method used to clear the context.
   * Take care of any side-effects that we have introduced during the operation.
   * Do not call this method directly - finish the operation or at least use try with resources
   */
  protected abstract void clear();

  public static void changeDefaultLayout(final Layout layout) {
    defaultLayout = layout;
  }
  public static void changeDefaultKeyRegex(final String pattern) {
    checkNotNull(pattern, "pass a valid regex");
    defaultKeyRegexPattern = Pattern.compile(pattern);
  }
  public static void changeDefaultKeyRegex(final KeyRegex keyRegex) {
    defaultKeyRegexPattern = Pattern.compile(keyRegex.getRegex());
  }
  public static void disableDefaultKeyValidation() {
    defaultKeyRegexPattern = null;
  }

  public void logDebug(final String debugMessage) {
    logDebug(debugMessage, Collections.emptyMap());
  }

  public abstract void logDebug(final String debugMessage, final Map<String, Object> keyValues);

  public OperationContext as(final Layout layout) {
    this.layout = layout;
    return this;
  }

  public OperationContext asJson() {
    this.layout = Layout.Json;
    return this;
  }

  public OperationContext asKeyValuePairs() {
    this.layout = Layout.KeyValuePair;
    return this;
  }

  public OperationContext disableKeyValidation() {
    keyRegexPattern = null;
    return this;
  }

  public OperationContext validate(final KeyRegex keyRegex) {
    keyRegexPattern = Pattern.compile(keyRegex.getRegex());
    return this;
  }

  public OperationContext validate(final String regex) {
    keyRegexPattern = Pattern.compile(regex);
    return this;
  }

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
    state.start(this);
    return this;
  }

  public void wasSuccessful() {
    state.succeed(this);
    clear();
  }

  public void wasSuccessful(final Object result) {
    with(Key.Result, result);
    state.succeed(this);
    clear();
  }

  public void wasSuccessful(final Object result, final Level level) {
    // TODO decide if we want to support different levels of result logs
  }

  public void wasFailure() {
    state.fail(this);
    clear();
  }

  public void wasFailure(final Exception e) {
    with(Key.ErrorMessage, e.getMessage());
    state.fail(this);
    clear();
  }

  public void wasFailure(final Object result) {
    with(Key.Result, result);
    state.fail(this);
    clear();
  }

  public void wasFailure(final Object result, final Level level) {
    // TODO decide if we want to support different levels of result logs
  }

  public void log(Level level) {
    log(null, level);
  }

  public void log(final Outcome outcome, final Level logLevel) {
    new LogFormatter(actorOrLogger).log(this, outcome, logLevel, layout);
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

  String getName() {
    return name;
  }

  Map<String, Object> getParameters() {
    return parameters.getParameters();
  }

  Object getActorOrLogger() {
    return actorOrLogger;
  }

  void changeState(OperationState operationState) {
    state = operationState;
  };

  void addParam(final String key, final Object value) {
    if (!Objects.isNull(keyRegexPattern) && !keyRegexPattern.matcher(key).matches()) {
      throw new AssertionError(key + " does not match " + keyRegexPattern.toString());
    }

    parameters.put(key, value);
  }

  void addParam(final Map<String, Object> keyValues) {
    parameters.putAll(keyValues);
  }

}
