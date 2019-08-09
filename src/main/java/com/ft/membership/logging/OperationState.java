package com.ft.membership.logging;

import java.util.Map;

public interface OperationState {
    // OperationState(final SimpleOperationContext operationContext);

    void with(final Map<String, Object> keyValues);
    void with(final String key, final Object value);

    String getType();

    void start();
    void succeed();
    void fail();
}
