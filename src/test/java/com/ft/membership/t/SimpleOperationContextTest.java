package com.ft.membership.t;

import static com.ft.membership.logging.SimpleOperationContext.operation;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.ft.membership.logging.OperationContext;
import com.ft.membership.logging.SimpleOperationContext;
import java.util.Collections;
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
  public void start_successful_operation() throws Exception {
    SimpleOperationContext.operation("compound_success", mockLogger).started().wasSuccessful();

    verify(mockLogger, times(2)).isInfoEnabled();

    verify(mockLogger).info("operation=\"compound_success\" operationState=\"started\"");
    verify(mockLogger)
        .info("operation=\"compound_success\" outcome=\"success\" operationState=\"success\"");
    verifyNoMoreInteractions(mockLogger);
  }

  @Test
  public void start_failure_operation() throws Exception {
    SimpleOperationContext.operation("compound_success", mockLogger).started().wasFailure();

    verify(mockLogger, times(1)).isInfoEnabled();
    verify(mockLogger, times(1)).isErrorEnabled();

    verify(mockLogger).info("operation=\"compound_success\" operationState=\"started\"");
    verify(mockLogger)
        .error("operation=\"compound_success\" outcome=\"failure\" operationState=\"fail\"");
    verifyNoMoreInteractions(mockLogger);
  }

  @Test
  public void multiple_operation_states() throws Exception {
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
            "operation=\"getUserSubscriptions\" outcome=\"success\" userId=\"1234\" operationState=\"success\" activeSubscription=\"S-12345\"");
    verifyNoMoreInteractions(mockLogger);
  }

  @Test
  public void debug_capabilities() throws Exception {
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
            "operation=\"getUserSubscriptions\" outcome=\"success\" userId=\"1234\" operationState=\"success\" activeSubscription=\"S-12345\"");
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
  public void create_action_with_no_operation() throws Exception {
    SimpleOperationContext.action("compound_action", mockLogger).started().wasSuccessful();

    verify(mockLogger, times(2)).isInfoEnabled();
    verify(mockLogger).info("action=\"compound_action\" operationState=\"started\"");
    verify(mockLogger)
        .info("action=\"compound_action\" outcome=\"success\" operationState=\"success\"");
    verifyNoMoreInteractions(mockLogger);
  }

  @Test
  public void create_action_with_operation() throws Exception {
    OperationContext operation =
        SimpleOperationContext.operation("compound_operation", mockLogger).started();
    doAction();
    operation.wasSuccessful();

    verify(mockLogger, times(4)).isInfoEnabled();
    verify(mockLogger).info("operation=\"compound_operation\" operationState=\"started\"");
    verify(mockLogger)
        .info(
            "action=\"compound_action\" operation=\"compound_operation\" operationState=\"started\"");
    verify(mockLogger)
        .info(
            "action=\"compound_action\" outcome=\"success\" operation=\"compound_operation\" operationState=\"success\"");
    verify(mockLogger)
        .info("operation=\"compound_operation\" outcome=\"success\" operationState=\"success\"");

    verifyNoMoreInteractions(mockLogger);
  }

  private void doAction() {
    SimpleOperationContext.action("compound_action", mockLogger).started().wasSuccessful();
  }

  @Test
  public void finish_not_started_operation() throws Exception {
    OperationContext operation =
        SimpleOperationContext.operation("compound_operation", mockLogger);
    doAction();
    operation.wasSuccessful();

    verify(mockLogger, times(3)).isInfoEnabled();
    verify(mockLogger)
        .info(
            "action=\"compound_action\" operation=\"compound_operation\" operationState=\"started\"");
    verify(mockLogger)
        .info(
            "action=\"compound_action\" outcome=\"success\" operation=\"compound_operation\" operationState=\"success\"");
    verify(mockLogger)
        .info("operation=\"compound_operation\" outcome=\"success\" operationState=\"success\"");

    verifyNoMoreInteractions(mockLogger);
  }

  @Test
  public void not_finished_operation() throws Exception {
    try (OperationContext operation =
        SimpleOperationContext.operation("compound_operation", mockLogger).started()) {
      doAction();
    }

    verify(mockLogger, times(3)).isInfoEnabled();
    verify(mockLogger, times(1)).isErrorEnabled();

    verify(mockLogger).info("operation=\"compound_operation\" operationState=\"started\"");
    verify(mockLogger)
        .info(
            "action=\"compound_action\" operation=\"compound_operation\" operationState=\"started\"");
    verify(mockLogger)
        .info(
            "action=\"compound_action\" outcome=\"success\" operation=\"compound_operation\" operationState=\"success\"");
    verify(mockLogger)
        .error("operation=\"compound_operation\" outcome=\"failure\" operationState=\"fail\" "
            + "result=\"Programmer error: operation auto-closed before wasSuccessful() or wasFailure() called.\"");

    verifyNoMoreInteractions(mockLogger);
  }
}
