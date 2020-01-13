package com.ft.membership.t;

import static com.ft.membership.logging.OperationContext.disableDefaultKeyValidation;
import static com.ft.membership.logging.SimpleOperationContext.action;
import static com.ft.membership.logging.SimpleOperationContext.operation;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.ft.membership.logging.KeyRegex;
import com.ft.membership.logging.Layout;
import com.ft.membership.logging.OperationContext;
import com.ft.membership.logging.SimpleOperationContext;
import java.util.Collections;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.event.Level;

@RunWith(MockitoJUnitRunner.class)
public class SimpleOperationContextTest {

  @Mock Logger mockLogger;

  @Before
  public void setup() {
    Mockito.when(mockLogger.isInfoEnabled()).thenReturn(true);
    Mockito.when(mockLogger.isErrorEnabled()).thenReturn(true);
    Mockito.when(mockLogger.isDebugEnabled()).thenReturn(true);
    Mockito.when(mockLogger.isWarnEnabled()).thenReturn(true);


    SimpleOperationContext.changeDefaultLevel(Level.INFO);
    SimpleOperationContext.changeDefaultLayout(Layout.KeyValuePair);
    SimpleOperationContext.changeDefaultKeyRegex(KeyRegex.CamelCase);
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
  public void success_with_result() {
    operation("compound_success", mockLogger).started().wasSuccessful("42");

    verify(mockLogger, times(2)).isInfoEnabled();

    verify(mockLogger).info("operation=\"compound_success\" operationState=\"started\"");
    verify(mockLogger)
        .info(
            "operation=\"compound_success\" operationState=\"success\" result=\"42\" outcome=\"success\"");
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
    OperationContext operation =
        operation("getUserSubscriptions", mockLogger).with("userId", "1234").started();

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

  @Test
  public void as_layout() {
    SimpleOperationContext.changeDefaultLayout(Layout.Json);
    operation("simple_op", mockLogger).as(Layout.KeyValuePair).started().wasSuccessful();

    verify(mockLogger, times(2)).isInfoEnabled();
    verify(mockLogger).info("operation=\"simple_op\" operationState=\"started\"");
  }

  @Test
  public void simple_json_layout() {
    operation("compound_success", mockLogger).asJson().started().wasSuccessful();

    verify(mockLogger, times(2)).isInfoEnabled();

    ArgumentCaptor<String> lines = ArgumentCaptor.forClass(String.class);
    verify(mockLogger, times(2)).info(lines.capture());

    final String line1 = lines.getAllValues().get(0);
    assert line1.contains("\"logLevel\":\"INFO\"") : line1 + " must contain logLevel";
    assert line1.contains("\"operation\":\"compound_success\"") : line1 + " must contain operation";
    assert line1.contains("\"operationState\":\"started\"") : line1 + " must contain operationState";
    assert !line1.contains("\"outcome\":\"success\"") : line1 + " must not contain outcome";

    final String line2 = lines.getAllValues().get(1);
    assert line2.contains("\"logLevel\":\"INFO\"") : line2 + " must contain logLevel";
    assert line2.contains("\"operation\":\"compound_success\"") : line2 + " must contain operation";
    assert line2.contains("\"operationState\":\"success\"") : line2 + " must contain operationState";
    assert line2.contains("\"outcome\":\"success\"") : line2 + " must contain outcome";

    verifyNoMoreInteractions(mockLogger);
  }


  @Test
  public void change_default_layout_to_json() {
    SimpleOperationContext.changeDefaultLayout(Layout.Json);
    operation("simple_op", mockLogger).started().wasSuccessful();

    verify(mockLogger, times(2)).isInfoEnabled();

    ArgumentCaptor<String> lines = ArgumentCaptor.forClass(String.class);
    verify(mockLogger, times(2)).info(lines.capture());

    final String line1 = lines.getAllValues().get(0);
    assert line1.contains("\"logLevel\":\"INFO\"") : line1 + " must contain logLevel";
  }

  @Test
  public void change_layout_to_key_value_for_operation() {
    SimpleOperationContext.changeDefaultLayout(Layout.Json);
    operation("simple_op", mockLogger).asKeyValuePairs().started().wasSuccessful();

    verify(mockLogger, times(2)).isInfoEnabled();
    verify(mockLogger).info("operation=\"simple_op\" operationState=\"started\"");
  }

  @Test
  public void validate_valid_key() {
    operation("simple_op", mockLogger).validate(KeyRegex.CamelCase).started().wasSuccessful();
  }

  @Test
  public void validate_valid_parameters() {
    SimpleOperationContext.changeDefaultKeyRegex(KeyRegex.CamelCase);
    operation("simple_op", mockLogger)
        .started()
        .with("activeSubscription", "S-12345")
        .wasSuccessful();
  }

  @Test(expected = AssertionError.class)
  public void validate_invalid_key() {
    SimpleOperationContext.changeDefaultKeyRegex(KeyRegex.CamelCase);
    operation("simple_op", mockLogger).started().with("InvalidKey", "1").wasSuccessful();
  }

  @Test
  public void disable_default_key_validation() {
    SimpleOperationContext.disableDefaultKeyValidation();
    operation("simple_op", mockLogger).started().with("InvalidKey", "1").wasSuccessful();
  }

  @Test
  public void disable_key_validation_for_operation() {
    operation("simple_op", mockLogger).disableKeyValidation()
        .started().with("InvalidKey", "1").wasSuccessful();
  }

  @Test(expected = AssertionError.class)
  public void validate_per_operation() {
    disableDefaultKeyValidation();

    operation("simple_op", mockLogger).validate(KeyRegex.CamelCase)
        .started().with("InvalidKey", "1").wasSuccessful();
  }

  // Not supported yet
  @Test
  @Ignore
  public void validate_per_operation_with_custom_pattern() {
    disableDefaultKeyValidation();

    operation("simple_op", mockLogger)
        // .validate("\"([A-Z]+[a-z]+\\\\w+)+\"")
        .started()
        .with("InvalidKey", "1")
        .wasSuccessful();
  }

  @Test()
  public void at_warn_level() {
    operation("compound_success", mockLogger).at(Level.WARN).started().wasSuccessful();

    verify(mockLogger, times(2)).isWarnEnabled();

    verify(mockLogger).warn("operation=\"compound_success\" operationState=\"started\"");
    verify(mockLogger)
        .warn("operation=\"compound_success\" operationState=\"success\" outcome=\"success\"");
    verifyNoMoreInteractions(mockLogger);
  }

  @Test
  public void at_different_level() {
    operation("compound_success", mockLogger).started().at(Level.WARN).wasSuccessful();

    verify(mockLogger, times(1)).isInfoEnabled();
    verify(mockLogger, times(1)).isWarnEnabled();

    verify(mockLogger).info("operation=\"compound_success\" operationState=\"started\"");
    verify(mockLogger)
        .warn("operation=\"compound_success\" operationState=\"success\" outcome=\"success\"");
    verifyNoMoreInteractions(mockLogger);
  }

  @Test
  public void at_same_level() {
    operation("compound_success", mockLogger).started().at(Level.INFO).wasSuccessful();

    verify(mockLogger, times(2)).isInfoEnabled();

    verify(mockLogger).info("operation=\"compound_success\" operationState=\"started\"");
    verify(mockLogger)
        .info("operation=\"compound_success\" operationState=\"success\" outcome=\"success\"");
    verifyNoMoreInteractions(mockLogger);
  }

  @Test
  public void at_disabled_level() {
    Mockito.when(mockLogger.isTraceEnabled()).thenReturn(false);

    operation("compound_success", mockLogger).started().at(Level.TRACE).wasSuccessful();

    verify(mockLogger, times(1)).isInfoEnabled();
    verify(mockLogger, times(1)).isTraceEnabled();

    verify(mockLogger).info("operation=\"compound_success\" operationState=\"started\"");
    verifyNoMoreInteractions(mockLogger);
  }

  @Test
  public void change_default_level() {
    SimpleOperationContext.changeDefaultLevel(Level.WARN);
    operation("compound_success", mockLogger).started().wasSuccessful();

    verify(mockLogger, times(2)).isWarnEnabled();

    verify(mockLogger).warn("operation=\"compound_success\" operationState=\"started\"");
    verify(mockLogger)
        .warn("operation=\"compound_success\" operationState=\"success\" outcome=\"success\"");
    verifyNoMoreInteractions(mockLogger);
  }
}