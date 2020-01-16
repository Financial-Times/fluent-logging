package com.ft.membership.logging;

import static com.ft.membership.logging.Preconditions.checkIsEmpty;

import java.util.Map;
import java.util.Objects;
import org.slf4j.MDC;
import org.slf4j.event.Level;

public final class SimpleFluentLogger extends FluentLogger {
  private enum Type {
    action,
    operation
  }

  private Type type;

  private SimpleFluentLogger(
      final String name,
      final Type type,
      final Object actorOrLogger,
      final Map<String, Object> parameters) {
    checkIsEmpty(name, "provide a valid name");

    this.name = name;
    this.type = type;
    this.actorOrLogger = actorOrLogger;
    this.parameters = Parameters.parameters(parameters);
  }

  public static SimpleFluentLogger operation(final String name, final Object actorOrLogger) {
    final SimpleFluentLogger context =
        new SimpleFluentLogger(name, Type.operation, actorOrLogger, null);
    context.changeState(OperationConstructedState.from(context));
    context.with(Key.Operation, name);
    return context;
  }

  public static SimpleFluentLogger action(final String name, final Object actorOrLogger) {
    final SimpleFluentLogger context =
        new SimpleFluentLogger(name, Type.action, actorOrLogger, null);
    context.changeState(ActionConstructedState.from(context));

    final String operation = MDC.get("operation");
    if (Objects.nonNull(operation) && !operation.isEmpty()) {
      context.with(Key.Operation, operation);
    }

    context.with(Key.Action, name);

    return context;
  }

  public void logDebug(final String debugMessage, final Map<String, Object> keyValues) {
    SimpleFluentLogger debugSimpleOperationContext = getIsolatedOperationContext();

    debugSimpleOperationContext.with(Key.DebugMessage, debugMessage);
    debugSimpleOperationContext.with(keyValues);

    debugSimpleOperationContext.log(null, Level.DEBUG);
  }

  // Needed for linking operations with actions
  void addIdentity(final String name) {
    MDC.put("operation", name);
  }

  private Type getType() {
    return type;
  }

  @Override
  protected void clear() {
    if (state != null && Type.operation.equals(type)) {
      MDC.remove("operation");
    }
  }

  @Override
  public String toString() {
    return "name=" + getName() + " type=" + getType() + " state=" + getState();
  }

  private SimpleFluentLogger getIsolatedOperationContext() {
    SimpleFluentLogger debugSimpleOperationContext =
        new SimpleFluentLogger(name, type, actorOrLogger, parameters.getParameters());

    IsolatedState isolatedState = IsolatedState.from(this);
    debugSimpleOperationContext.changeState(isolatedState);
    return debugSimpleOperationContext;
  }
}
