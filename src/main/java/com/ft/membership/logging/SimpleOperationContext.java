package com.ft.membership.logging;

import static com.ft.membership.logging.Preconditions.checkIsEmpty;
import static com.ft.membership.logging.Preconditions.checkNotNull;

import java.util.Map;
import java.util.Objects;
import org.slf4j.MDC;
import org.slf4j.event.Level;

public final class SimpleOperationContext extends OperationContext {
  private String type;

  private SimpleOperationContext(
      final String name,
      final String type,
      final Object actorOrLogger,
      final Map<String, Object> parameters
  ) {
    checkIsEmpty(name, "provide a valid name");

    this.name = name;
    this.type = type;
    this.actorOrLogger = actorOrLogger;
    this.parameters = Parameters.parameters(parameters);
  }

  public static SimpleOperationContext operation(final String name, final Object actorOrLogger) {
    final SimpleOperationContext context =
        new SimpleOperationContext(name, "operation", actorOrLogger, null);
    context.changeState(OperationConstructedState.from(context));
    context.with(Key.Operation, name);
    return context;
  }

  public static SimpleOperationContext action(final String name, final Object actorOrLogger) {
    final SimpleOperationContext context =
        new SimpleOperationContext(name, "action", actorOrLogger, null);
    context.changeState(ActionConstructedState.from(context));

    final String operation = MDC.get("operation");
    if (Objects.nonNull(operation) && !operation.isEmpty()) {
      context.with(Key.Operation, operation);
    }

    context.with(Key.Action, name);

    return context;
  }

  public void logDebug(final String debugMessage, final Map<String, Object> keyValues) {
    checkNotNull(state, "operation is already closed");

    SimpleOperationContext debugSimpleOperationContext = getIsolatedOperationContext();

    debugSimpleOperationContext.with(Key.DebugMessage, debugMessage);
    debugSimpleOperationContext.with(keyValues);

    debugSimpleOperationContext.log(null, Level.DEBUG);
  }

  // Needed for linking operations with actions
  void addIdentity(final String name) {
    MDC.put("operation", name);
  }

  @Override
  protected void clear() {
    if (state != null && "operation".equals(type)) {
      MDC.remove("operation");
    }

    // We need to clear the reference
    state = null;
  }

  private SimpleOperationContext getIsolatedOperationContext() {
    SimpleOperationContext debugSimpleOperationContext = new SimpleOperationContext(
        name,
        type,
        actorOrLogger,
        parameters.getParameters()
    );

    IsolatedState isolatedState = IsolatedState.from(this);
    debugSimpleOperationContext.changeState(isolatedState);
    return debugSimpleOperationContext;
  }
}
