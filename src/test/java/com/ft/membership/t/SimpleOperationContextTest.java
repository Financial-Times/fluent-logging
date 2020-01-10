package com.ft.membership.t;

import static com.ft.membership.logging.SimpleOperationContext.action;
import static com.ft.membership.logging.SimpleOperationContext.operation;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.ft.membership.logging.OperationContext;
import java.util.Collections;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class SimpleOperationContextTest {

  @Mock Logger mockLogger;

  @Before
  public void setup() {
    Mockito.when(mockLogger.isInfoEnabled()).thenReturn(true);
    Mockito.when(mockLogger.isErrorEnabled()).thenReturn(true);
    Mockito.when(mockLogger.isDebugEnabled()).thenReturn(true);
  }

  @Test
  public void start_successful_operation() {
    operation("compound_success", mockLogger).started().wasSuccessful();

    verify(mockLogger, times(2)).isInfoEnabled();

    verify(mockLogger).info("operation=\"compound_success\" operationState=\"started\"");
    verify(mockLogger)
        .info("operation=\"compound_success\" operationState=\"success\" outcome=\"success\"");
    verifyNoMoreInteractions(mockLogger);
  }

  @Test
  public void start_failure_operation() {
    operation("compound_success", mockLogger).started().wasFailure();

    verify(mockLogger, times(1)).isInfoEnabled();
    verify(mockLogger, times(1)).isErrorEnabled();

    verify(mockLogger).info("operation=\"compound_success\" operationState=\"started\"");
    verify(mockLogger)
        .error("operation=\"compound_success\" operationState=\"fail\" outcome=\"failure\"");
    verifyNoMoreInteractions(mockLogger);
  }

  @Test
  public void exception_failure_operation() {
    operation("compound_success", mockLogger)
        .wasFailure(new IllegalStateException("exception_failure_operation"));

    verify(mockLogger, times(1)).isErrorEnabled();
    verify(mockLogger)
        .error(
            "operation=\"compound_success\" errorMessage=\"exception_failure_operation\""
                + " operationState=\"fail\" outcome=\"failure\"");
    verifyNoMoreInteractions(mockLogger);
  }

  @Test
  public void multiple_operation_params() {
    HashMap params = new HashMap();
    params.put("activeSubscription", "S-12345");
    params.put("userId", "1234");

    operation("getUserSubscriptions", mockLogger).with(params).started().wasSuccessful();
    verify(mockLogger, times(2)).isInfoEnabled();
    verify(mockLogger)
        .info(
            "operation=\"getUserSubscriptions\" userId=\"1234\" activeSubscription=\"S-12345\" operationState=\"started\"");
    verify(mockLogger)
        .info(
            "operation=\"getUserSubscriptions\" userId=\"1234\" activeSubscription=\"S-12345\" operationState=\"success\" outcome=\"success\"");
  }

  @Test
  public void multiple_operation_states() {
    OperationContext operation = operation("getUserSubscriptions", mockLogger)
        .with("userId", "1234").started();

    operation.logDebug("The user has a lot of subscriptions");
    operation.with("activeSubscription", "S-12345");
    operation.wasSuccessful();

    verify(mockLogger, times(2)).isInfoEnabled();
    verify(mockLogger, times(1)).isDebugEnabled();
    verify(mockLogger)
        .info("operation=\"getUserSubscriptions\" userId=\"1234\" operationState=\"started\"");
    verify(mockLogger)
        .debug(
            "operation=\"getUserSubscriptions\" userId=\"1234\" operationState=\"started\" debugMessage=\"The user has a lot of subscriptions\"");
    verify(mockLogger)
        .info(
            "operation=\"getUserSubscriptions\" userId=\"1234\" operationState=\"success\" activeSubscription=\"S-12345\" outcome=\"success\"");
    verifyNoMoreInteractions(mockLogger);
  }

  @Test
  public void debug_capabilities() {
    OperationContext operation = operation("getUserSubscriptions", mockLogger);
    operation.with("userId", "1234").started();
    operation.logDebug(
        "The user has a lot of subscriptions", Collections.singletonMap("subscriptionCount", 999));

    operation.with("activeSubscription", "S-12345");
    operation.wasSuccessful();

    verify(mockLogger, times(2)).isInfoEnabled();
    verify(mockLogger, times(1)).isDebugEnabled();
    verify(mockLogger)
        .info("operation=\"getUserSubscriptions\" userId=\"1234\" operationState=\"started\"");
    verify(mockLogger)
        .debug(
            "operation=\"getUserSubscriptions\" userId=\"1234\" operationState=\"started\""
                + " debugMessage=\"The user has a lot of subscriptions\" subscriptionCount=999");
    verify(mockLogger)
        .info(
            "operation=\"getUserSubscriptions\" userId=\"1234\" operationState=\"success\" activeSubscription=\"S-12345\" outcome=\"success\"");
    verifyNoMoreInteractions(mockLogger);
  }

  @Test(expected = NullPointerException.class)
  public void debug_when_finished_operation() {
    OperationContext operation = operation("getUserSubscriptions", mockLogger).started();
    operation.wasSuccessful();
    operation.logDebug(
        "The user has a lot of subscriptions", Collections.singletonMap("subscriptionCount", 999));
  }

  @Test
  public void create_action_with_no_operation() {
    action("compound_action", mockLogger).started().wasSuccessful();

    verify(mockLogger, times(2)).isInfoEnabled();
    verify(mockLogger).info("action=\"compound_action\" operationState=\"started\"");
    verify(mockLogger)
        .info("action=\"compound_action\" operationState=\"success\" outcome=\"success\"");
    verifyNoMoreInteractions(mockLogger);
  }

  @Test
  public void create_action_with_operation() {
    OperationContext operation = operation("compound_operation", mockLogger).started();
    doAction();
    operation.wasSuccessful();

    verify(mockLogger, times(4)).isInfoEnabled();
    verify(mockLogger).info("operation=\"compound_operation\" operationState=\"started\"");
    verify(mockLogger)
        .info(
            "operation=\"compound_operation\" action=\"compound_action\" operationState=\"started\"");
    verify(mockLogger)
        .info(
            "operation=\"compound_operation\" action=\"compound_action\" operationState=\"success\" outcome=\"success\"");
    verify(mockLogger)
        .info("operation=\"compound_operation\" operationState=\"success\" outcome=\"success\"");

    verifyNoMoreInteractions(mockLogger);
  }

  private void doAction() {
    action("compound_action", mockLogger).started().wasSuccessful();
  }

  @Test
  public void finish_not_started_operation() {
    OperationContext operation = operation("compound_operation", mockLogger);
    doAction();
    operation.wasSuccessful();

    verify(mockLogger, times(3)).isInfoEnabled();
    verify(mockLogger)
        .info(
            "operation=\"compound_operation\" action=\"compound_action\" operationState=\"started\"");
    verify(mockLogger)
        .info(
            "operation=\"compound_operation\" action=\"compound_action\" operationState=\"success\" outcome=\"success\"");
    verify(mockLogger)
        .info("operation=\"compound_operation\" operationState=\"success\" outcome=\"success\"");

    verifyNoMoreInteractions(mockLogger);
  }

  @Test
  public void not_finished_operation() {
    try (OperationContext operation = operation("compound_operation", mockLogger).started()) {
      doAction();
    }

    verify(mockLogger, times(3)).isInfoEnabled();
    verify(mockLogger, times(1)).isErrorEnabled();

    verify(mockLogger).info("operation=\"compound_operation\" operationState=\"started\"");
    verify(mockLogger)
        .info(
            "operation=\"compound_operation\" action=\"compound_action\" operationState=\"started\"");
    verify(mockLogger)
        .info(
            "operation=\"compound_operation\" action=\"compound_action\" operationState=\"success\" outcome=\"success\"");
    verify(mockLogger)
        .error(
            "operation=\"compound_operation\" operationState=\"fail\" "
                + "result=\"Programmer error: operation auto-closed before wasSuccessful() or wasFailure() called.\" "
                + "outcome=\"failure\"");

    verifyNoMoreInteractions(mockLogger);
  }
}
