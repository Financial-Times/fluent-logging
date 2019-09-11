package com.ft.membership.logging;

import java.util.Map;
import java.util.Optional;

import static com.ft.membership.logging.Preconditions.checkNotNull;

/**
 * An Operation is a logging context with starting parameters, which either succeeds or fails, supporting additional
 * detail as to what happened. It logs in a <tt>key="value"</tt> format suitable for parsing by a log aggregator such as
 * Splunk, escaping quotes with back-slashes.
 *
 * <p>e.g.</p>
 *
 * <pre>
 *     Operation operation = Operation.operation("launch")
 *         .with("probe", probe)
 *         .with("target", target)
 *         .started(this);
 *
 *     try {
 *         Flight f = probe.launch();
 *
 *         operation.wasSuccessful()
 *             .yielding("position", f.position())
 *             .log();
 *
 *     } catch(LaunchException e) {
 *         operation.wasFailure()
 *             .throwingException(e)
 *             .withMessage(e.reason())
 *             .withDetail("malfunction", e.malfunction())
 *             .log();
 *     }
 * </pre>
 * On success, this would log something like:
 * <pre>
 *     INFO operation="launch" probe=27 target="Mars"
 *     ...
 *     INFO operation="launch" probe=27 target="Mars" position="[10.0,17.2,0.0]" outcome="success"
 * </pre>
 *
 * or on failure:
 * <pre>
 *     INFO operation="launch" probe=27 target="Mars"
 *     ...
 *     ERROR operation="launch" probe=27 target="Mars" outcome="failure" message="crash" malfunction="parachute" exception="LaunchException"
 *     LaunchException: exception message
 *     ...stacktrace...
 * </pre>
 *
 */
public class Operation implements AutoCloseable {

    private final String operationName;
    private final boolean layout;
    private Object actorOrLogger;

    private boolean terminated;

    private Map<String, Object> parameters;

    /**
     * create an Operation, ready for decoration with parameters.
     * @param operation name of the operation.
     * @return an Operation.
     */
    public static OperationBuilder operation(final String operation) {
        return new OperationBuilder(operation);
    }

    public Operation(final String operationName, final Object actorOrLogger, final Map<String, Object> parameters, final boolean layout) {
        this.operationName = operationName;
        this.actorOrLogger = actorOrLogger;
        this.parameters = parameters;
        this.layout = layout;
    }

    public static class OperationBuilder extends Parameters {

        private final String operationName;
        private boolean layout;

        OperationBuilder(final String operationName){
            checkNotNull(operationName, "require operationName");
            this.operationName = operationName;
        }


        /**
         * add a starting parameter.
         * @param key a log key
         * @param value a value
         * @return Operation
         */
        public OperationBuilder with(final Key key, final Object value) {
            return with(key.getKey(), value);
        }

        /**
         * add a starting parameter.
         * @param key a log key string
         * @param value a value
         * @return Operation
         */
        public OperationBuilder with(final String key, final Object value) {
            put(key, value);
            return this;
        }

        public OperationBuilder jsonLayout() {
            this.layout = true;
            return this;
        }

        /**
         * add starting parameters from entries in a map.
         * @param keyValues a map of parameter key-values
         * @return Operation
         */
        public OperationBuilder with(final Map<String, Object> keyValues) {
            putAll(keyValues);
            return this;
        }

        /**
         * mark the start of an operation, logging starting parameters at <tt>INFO</tt> level.
         * @param actorOrLogger object for logging context; may be an instance of {@link org.slf4j.Logger},
         *                      in which case that logger will be used for logging, or an object in which case
         *                      a logger will be obtained by passing that object to {@link org.slf4j.LoggerFactory}.
         *
         * @return Operation
         */
        public Operation started(final Object actorOrLogger) {
            final Operation operation = new Operation(operationName, actorOrLogger, getParameters(), layout);
            new LogFormatter(actorOrLogger).logStart(operation);
            return operation;
        }

        /**
         * mark the start of an operation, but do not log starting parameters, for less noise.
         * <p>parameters will still be logged on a termination, e.g. on {@link #wasSuccessful()}.</p>
         * @param actorOrLogger object for logging context; may be an instance of {@link org.slf4j.Logger},
         *                      in which case that logger will be used for logging, or an object in which case
         *                      a logger will be obtained by passing that object to {@link org.slf4j.LoggerFactory}.
         * @return Operation
         */
        public Operation initiate(final Object actorOrLogger) {
            return new Operation(operationName, actorOrLogger, getParameters(), layout);
        }
    }

    /**
     * mark the operation as successful, and prepare to log.
     * @return a Yield to be decorated and logged.
     */
    public Yield wasSuccessful() {
        return new Yield(this);
    }

    /**
     * mark the operation as a failure, and prepare to log.
     * @return a Failure to be decorated and logged.
     */
    public Failure wasFailure() {
        return new Failure(this);
    }

    void terminated() {
        this.terminated = true;
    }

    String getName() {
        return operationName;
    }

    Map<String, Object> getParameters() {
        return parameters;
    }

    Object getActorOrLogger() {
        return actorOrLogger;
    }

    @Override
    public void close() {
        if (!terminated) {
            this.wasFailure()
                    .throwingException(new IllegalStateException("Programmer error: operation auto-closed before wasSuccessful() or wasFailure() called.")) // so we at least get a stack-trace
                    .log(Optional.ofNullable(actorOrLogger).orElse(this));
        }
    }

    public boolean isJsonLayout() {
        return layout;
    }

}
