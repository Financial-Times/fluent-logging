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
    context.setState(new OperationConstructedState(context));
    return context;
  }

  public static SimpleOperationContext action(final String name, final Object actorOrLogger) {
    checkNotNull(name, "require name");
    final SimpleOperationContext context = new SimpleOperationContext(name, actorOrLogger, null);
    context.setState(new ActionConstructedState(context));

    final String operation = MDC.get("operation");
    if (Objects.nonNull(operation) && !operation.isEmpty()) {
      context.with(Key.Operation, operation);
    }

    return context;
  }

  public void logDebug(final String debugMessage, final Map<String, Object> keyValues) {
    checkNotNull(state, "operation is already closed");

    SimpleOperationContext debugSimpleOperationContext = new SimpleOperationContext(
        name,
        actorOrLogger,
        parameters.getParameters()
    );

    IsolatedState isolatedState = IsolatedState.from(debugSimpleOperationContext, state.getType());
    debugSimpleOperationContext.setState(isolatedState);

    debugSimpleOperationContext.with(Key.DebugMessage, debugMessage);
    debugSimpleOperationContext.with(keyValues);

    new LogFormatter(actorOrLogger).log(debugSimpleOperationContext, null, Level.DEBUG);
  }

  @Override
  protected void clear() {
    if (state != null && state.getType() == "operation") {
      MDC.remove("operation");
    }

    // We need to clear the reference
    state = null;
  }

  // Needed for linking operations with actions
  void addIdentity(final String name) {
    MDC.put("operation", name);
  }

  private void logError(Object actorOrLogger) {
    new LogFormatter(actorOrLogger).log(this, Outcome.Failure, Level.ERROR);
  }

  private void logInfo(Object actorOrLogger) {
    new LogFormatter(actorOrLogger).log(this, null, Level.INFO);
  }

}
