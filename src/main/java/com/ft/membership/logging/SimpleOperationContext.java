package com.ft.membership.logging;

import static com.ft.membership.logging.Preconditions.checkNotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
    final SimpleOperationContext context = new SimpleOperationContext(name, actorOrLogger, null);
    new OperationConstructedState(context);
    return context;
  }

  public static SimpleOperationContext action(final String name, final Object actorOrLogger) {
    final SimpleOperationContext context = new SimpleOperationContext(name, actorOrLogger, null);
    new ActionConstructedState(context);
    return context;
  }

  public void logDebug(final String debugMessage, final Map<String, Object> keyValues) {
    SimpleOperationContext debugSimpleOperationContext = new SimpleOperationContext(
        name,
        actorOrLogger,
        parameters.getParameters()
    );

    IsolatedState.from(debugSimpleOperationContext, this.state.getType());

    debugSimpleOperationContext.with(Key.DebugMessage, debugMessage);
    debugSimpleOperationContext.with(keyValues);

    new LogFormatter(actorOrLogger).log(debugSimpleOperationContext, null, Level.DEBUG);
  }

  @Override
  protected void clear() {
    if (state.getType() == "operation") {
      MDC.remove("operation");
    }
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
