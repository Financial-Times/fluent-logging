package com.ft.membership.logging;

import static com.ft.membership.logging.Preconditions.checkNotNull;

import java.util.Map;

import java.util.Objects;
import org.slf4j.MDC;
import org.slf4j.event.Level;

public final class SimpleOperationContext extends OperationContext {
  SimpleOperationContext(
      final String name,
      final Object actorOrLogger,
      final Map<String, Object> parameters
  ) {
    checkNotNull(name, "provide a name for the name");

    this.name = name;
    this.actorOrLogger = actorOrLogger;
    this.parameters = Parameters.parameters(parameters);
  }

  public static SimpleOperationContext operation(final String name, final Object actorOrLogger) {
    checkNotNull(name, "require name");
    final SimpleOperationContext context = new SimpleOperationContext(name, actorOrLogger, null);
    context.setState(OperationConstructedState.from(context));
    return context;
  }

  public static SimpleOperationContext action(final String name, final Object actorOrLogger) {
    checkNotNull(name, "require name");
    final SimpleOperationContext context = new SimpleOperationContext(name, actorOrLogger, null);
    context.setState(ActionConstructedState.from(context));

    final String operation = MDC.get("operation");
    if (Objects.nonNull(operation) && !operation.isEmpty()) {
      context.with(Key.Operation, operation);
    }

    return context;
  }

  public void logDebug(final String debugMessage, final Map<String, Object> keyValues) {
    checkNotNull(state, "operation is already closed");

    SimpleOperationContext debugSimpleOperationContext = getIsolatedOperationContext();

    debugSimpleOperationContext.with(Key.DebugMessage, debugMessage);
    debugSimpleOperationContext.with(keyValues);

    new LogFormatter(actorOrLogger).log(debugSimpleOperationContext, null, Level.DEBUG);
  }

  // Needed for linking operations with actions
  void addIdentity(final String name) {
    MDC.put("operation", name);
  }

  @Override
  protected void clear() {
    if (state != null && state.getType() == "operation") {
      MDC.remove("operation");
    }

    // We need to clear the reference
    state = null;
  }

  private SimpleOperationContext getIsolatedOperationContext() {
    SimpleOperationContext debugSimpleOperationContext = new SimpleOperationContext(
        name,
        actorOrLogger,
        parameters.getParameters()
    );

    IsolatedState isolatedState = IsolatedState.of(this, state.getType());
    debugSimpleOperationContext.setState(isolatedState);
    return debugSimpleOperationContext;
  }
}
