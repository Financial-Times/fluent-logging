package com.ft.membership.t;

import static com.ft.membership.logging.SimpleOperationContext.operation;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.ft.membership.logging.OperationContext;
import com.ft.membership.logging.SimpleOperationContext;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import java.util.Collections;

@RunWith(MockitoJUnitRunner.class)
public class SimpleOperationContextTest {

  @Mock
  Logger mockLogger;

  @Before
  public void setup() {
    Mockito.when(mockLogger.isInfoEnabled()).thenReturn(true);
    Mockito.when(mockLogger.isErrorEnabled()).thenReturn(true);
    Mockito.when(mockLogger.isDebugEnabled()).thenReturn(true);

    // SimpleOperationContext.setOperationIdentity(() -> "simpleTraceId");
  }

  @Test
  public void log_start_and_success() throws Exception {

    SimpleOperationContext.operation("compound_success", mockLogger).started().wasSuccessful();

    verify(mockLogger, times(2)).isInfoEnabled();
    verify(mockLogger).info("operation=\"compound_success\"");
    verify(mockLogger).info("operation=\"compound_success\" outcome=\"success\"");
    verifyNoMoreInteractions(mockLogger);
  }


  @Test
  public void compound_operation_should_have_fluent_api() throws Exception {
    OperationContext operation = operation("getUserSubscriptions", mockLogger)
        .with("userId", "1234")
        .started();

    operation.logDebug("The user has a lot of subscriptions");
    operation.with("activeSubscription", "S-12345");
    operation.wasSuccessful();

    verify(mockLogger, times(2)).isInfoEnabled();
    verify(mockLogger, times(1)).isDebugEnabled();
    verify(mockLogger).info("operation=\"getUserSubscriptions\" userId=\"1234\"");
    verify(mockLogger).debug(
        "operation=\"getUserSubscriptions\" userId=\"1234\" debugMessage=\"The user has a lot of subscriptions\"");
    verify(mockLogger).info(
        "operation=\"getUserSubscriptions\" outcome=\"success\" userId=\"1234\" activeSubscription=\"S-12345\"");
    verifyNoMoreInteractions(mockLogger);
  }

  @Test
  public void compound_operation_should_have_powerful_debug_capabilities() throws Exception {
    OperationContext operation = operation("getUserSubscriptions", mockLogger)
        .with("userId", "1234")
        .started();

    operation.logDebug(
        "The user has a lot of subscriptions",
        Collections.singletonMap("subscriptionCount", 999)
    );

    operation.with("activeSubscription", "S-12345");
    operation.wasSuccessful();

    verify(mockLogger, times(2)).isInfoEnabled();
    verify(mockLogger, times(1)).isDebugEnabled();
    verify(mockLogger).info("operation=\"getUserSubscriptions\" userId=\"1234\"");
    verify(mockLogger).debug(
        "operation=\"getUserSubscriptions\" userId=\"1234\" debugMessage=\"The user has a lot of subscriptions\" subscriptionCount=999");
    verify(mockLogger).info(
        "operation=\"getUserSubscriptions\" outcome=\"success\" userId=\"1234\" activeSubscription=\"S-12345\"");
    verifyNoMoreInteractions(mockLogger);
  }


  @Test
  public void log_simple_action() throws Exception {
    // SimpleOperationContext.setOperationIdentity(() -> null);
    SimpleOperationContext.action("compound_action", mockLogger).started().wasSuccessful();

    verify(mockLogger, times(2)).isInfoEnabled();
    verify(mockLogger).info("action=\"compound_action\"");
    verify(mockLogger).info("action=\"compound_action\" outcome=\"success\"");
    verifyNoMoreInteractions(mockLogger);
  }

  @Test
  @Ignore
  public void log_compound_operation_and_action() throws Exception {
    OperationContext operation = SimpleOperationContext
        .operation("compound_operation", mockLogger).started();

    SimpleOperationContext.action("compound_action", mockLogger).started().wasSuccessful();

    operation.wasSuccessful();
    verify(mockLogger, times(4)).isInfoEnabled();
    verify(mockLogger).info("operation=\"compound_operation\"");


    verify(mockLogger).info("action=\"compound_action\" operation=\"compound_operation\"");
    verify(mockLogger).info("action=\"compound_action\" outcome=\"success\" operation=\"compound_operation\"");

    verify(mockLogger).info("operation=\"compound_operation\" outcome=\"success\"");
    verifyNoMoreInteractions(mockLogger);
  }

}
