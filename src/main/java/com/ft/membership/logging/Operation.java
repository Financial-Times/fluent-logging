package com.ft.membership.logging;

import com.ft.membership.common.types.userid.UserId;
import com.google.common.base.Optional;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class Operation implements AutoCloseable {

    private final String operationName;
    private Object actorOrLogger;

    private boolean terminated;

    private Map<String, Object> parameters;

    public static OperationBuilder operation(final String operation) {
        return new OperationBuilder(operation);
    }

    public static ResultOperationBuilder resultOperation(final String operation) {
        return new ResultOperationBuilder(operation);
    }

    public Operation(final String operationName, final Object actorOrLogger, final Map<String, Object> parameters) {
        this.operationName = operationName;
        this.actorOrLogger = actorOrLogger;
        this.parameters = parameters;
    }

    public static class OperationBuilder extends Parameters {

        public static final String TIME_TO_COMPLETE = "timeToComplete";
        private final String operationName;
        private Object actorOrLogger;

        public OperationBuilder(final String operationName){
            checkNotNull(operationName, "require operationName");
            this.operationName = operationName;
        }

        public OperationBuilder with(final UserId id) {
            put(DomainObjectKey.UserId.getKey(), id);
            return this;
        }

        public OperationBuilder with(final DomainObjectKey key, final Object value) {
            return with(key.getKey(), value);
        }

        public OperationBuilder with(final String key, final Object value) {
            put(key, value);
            return this;
        }

        public OperationBuilder withTimeToComplete() {
            put(TIME_TO_COMPLETE, new TimeToComplete(System.currentTimeMillis()));
            return this;
        }

        public Operation started(final Object actorOrLogger) {
            final Operation operation = new Operation(operationName, actorOrLogger, getParameters());
            new LogFormatter(actorOrLogger).logInfo(operation);
            return operation;
        }
    }

    public static class ResultOperationBuilder extends OperationBuilder{

        public ResultOperationBuilder(final String operationName) {
            super(operationName);
        }

        @Override
        public Operation started(final Object actorOrLogger) {

            super.with(TIME_TO_COMPLETE, new TimeToComplete(System.currentTimeMillis()));
            return new Operation(super.operationName, actorOrLogger, getParameters());
        }
    }

    public Yield wasSuccessful() {
        return new Yield(this);
    }

    public Failure wasFailure() {
        return new Failure(this);
    }

    protected void terminated() {
        this.terminated = true;
    }

    protected String getName() {
        return operationName;
    }

    protected Map<String, Object> getParameters() {
        return parameters;
    }

    protected Object getActorOrLogger() {
        return actorOrLogger;
    }

    @Override
    public void close() {
        if (!terminated) {
            this.wasFailure()
                    .throwingException(new RuntimeException("operation auto-closed")) // so we at least get a stack-trace
                    .log(Optional.fromNullable(actorOrLogger).or(this));
        }
    }

}
