package com.ft.membership.t;

import static com.ft.membership.logging.FluentLogger.disableDefaultKeyValidation;
import static com.ft.membership.logging.SimpleFluentLogger.action;
import static com.ft.membership.logging.SimpleFluentLogger.operation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.ft.membership.logging.FluentLogger;
import com.ft.membership.logging.Key;
import com.ft.membership.logging.KeyRegex;
import com.ft.membership.logging.Layout;
import com.ft.membership.logging.Preconditions.EmptyValueException;
import com.ft.membership.logging.SimpleFluentLogger;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
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
public class SimpleFluentLoggerTest {

  @Mock Logger mockLogger;

  @Before
  public void setup() {
    Mockito.when(mockLogger.isInfoEnabled()).thenReturn(true);
    Mockito.when(mockLogger.isErrorEnabled()).thenReturn(true);
    Mockito.when(mockLogger.isDebugEnabled()).thenReturn(true);
    Mockito.when(mockLogger.isWarnEnabled()).thenReturn(true);

    SimpleFluentLogger.changeDefaultLevel(Level.INFO);
    SimpleFluentLogger.changeDefaultLayout(Layout.KeyValuePair);
    SimpleFluentLogger.changeDefaultKeyRegex(KeyRegex.CamelCase);
  }

  @Test
  public void start_successful_operation() {
    operation("compound_success", mockLogger).started().wasSuccessful();

    verify(mockLogger, times(2)).isInfoEnabled();

    verify(mockLogger).info("operation=\"compound_success\" loggerState=\"started\"");
    verify(mockLogger)
        .info("operation=\"compound_success\" loggerState=\"success\" outcome=\"success\"");
    verifyNoMoreInteractions(mockLogger);
  }

  @Test
  public void success_with_result() {
    operation("compound_success", mockLogger).started().wasSuccessful("42");

    verify(mockLogger, times(2)).isInfoEnabled();

    verify(mockLogger).info("operation=\"compound_success\" loggerState=\"started\"");
    verify(mockLogger)
        .info(
            "operation=\"compound_success\" loggerState=\"success\" result=\"42\" outcome=\"success\"");
    verifyNoMoreInteractions(mockLogger);
  }

  @Test
  public void start_failure_operation() {
    operation("compound_success", mockLogger).started().wasFailure();

    verify(mockLogger, times(1)).isInfoEnabled();
    verify(mockLogger, times(1)).isErrorEnabled();

    verify(mockLogger).info("operation=\"compound_success\" loggerState=\"started\"");
    verify(mockLogger)
        .error("operation=\"compound_success\" loggerState=\"fail\" outcome=\"failure\"");
    verifyNoMoreInteractions(mockLogger);
  }

  @Test
  public void exception_failure_operation() {
    operation("compound_success", mockLogger)
        .wasFailure(new IllegalStateException("exception_failure_operation"));

    verify(mockLogger, times(1)).isErrorEnabled();
    verify(mockLogger)
        .error(
            "operation=\"compound_success\" exceptionType=\"java.lang.IllegalStateException\" errorMessage=\"exception_failure_operation\""
                + " loggerState=\"fail\" outcome=\"failure\"");
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
            "operation=\"getUserSubscriptions\" userId=\"1234\" activeSubscription=\"S-12345\" loggerState=\"started\"");
    verify(mockLogger)
        .info(
            "operation=\"getUserSubscriptions\" userId=\"1234\" activeSubscription=\"S-12345\" loggerState=\"success\" outcome=\"success\"");
  }

  @Test
  public void null_pointer_exception_failure_stack() {
    final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStreamCaptor));

    try {
      Object a = null;
      a.toString();
    } catch (Exception exception) {
      operation("null_pointer_exception_failure_stack", mockLogger).wasFailure(exception);
    }

    String out = outputStreamCaptor.toString();

    assertTrue(
        "Null pointer should cause printStackTrace",
        out.contains("java.lang.NullPointerException"));
    assertTrue(
        "Null pointer should cause printStackTrace",
        out.contains(
            "at com.ft.membership.t.SimpleFluentLoggerTest.null_pointer_exception_failure_stack"));
  }

  @Test
  public void no_null_pointer_exception_failure_stack_when_disabled() {
    final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStreamCaptor));

    SimpleFluentLogger.disableNullPointerStackTrace();

    try {
      Object a = null;
      a.toString();
    } catch (Exception exception) {
      operation("null_pointer_exception_failure_stack", mockLogger).wasFailure(exception);
    }

    String out = outputStreamCaptor.toString();

    assertFalse(
        "Null pointer should cause printStackTrace",
        out.contains("java.lang.NullPointerException"));
  }

  @Test
  public void conflict_param_name() {
    operation("getUserSubscriptions", mockLogger).with(Key.LoggerState, "overwrite").started();
    verify(mockLogger, times(1)).isInfoEnabled();
    verify(mockLogger).info("operation=\"getUserSubscriptions\" loggerState=\"started\"");
  }

  enum TestType {
    SubscriptionNumber("subscriptionNumber");
    private String id;

    TestType(String id) {
      this.id = id;
    }

    @Override
    public String toString() {
      return id;
    }
  };

  @Test
  public void with_custom_type() {
    operation("getUserSubscriptions", mockLogger).with(TestType.SubscriptionNumber, "a").started();
    verify(mockLogger, times(1)).isInfoEnabled();
    verify(mockLogger)
        .info(
            "operation=\"getUserSubscriptions\" subscriptionNumber=\"a\" loggerState=\"started\"");
  }

  @Test
  public void with_null_key() {
    TestType nil = null;
    try {
      operation("getUserSubscriptions", mockLogger).with(nil, "a").started();
    } catch (NullPointerException e) {
      assertEquals(e.getMessage(), "FluentLogger.with(key, value) requires non-null key");
      return;
    }
    fail();
  }

  @Test(expected = AssertionError.class)
  public void key_validation_for_multiple_operation_params() {
    HashMap params = new HashMap();
    params.put("validParam", "1");
    params.put("InvalidParam", "2");

    operation("getUserSubscriptions", mockLogger).validate(KeyRegex.CamelCase).with(params);
  }

  @Test
  public void multiple_operation_states() {
    FluentLogger operation =
        operation("getUserSubscriptions", mockLogger).with("userId", "1234").started();

    operation.logDebug("The user has a lot of subscriptions");
    operation.with("activeSubscription", "S-12345");
    operation.wasSuccessful();

    verify(mockLogger, times(2)).isInfoEnabled();
    verify(mockLogger, times(1)).isDebugEnabled();
    verify(mockLogger)
        .info("operation=\"getUserSubscriptions\" userId=\"1234\" loggerState=\"started\"");
    verify(mockLogger)
        .debug(
            "operation=\"getUserSubscriptions\" userId=\"1234\" loggerState=\"started\" debugMessage=\"The user has a lot of subscriptions\"");
    verify(mockLogger)
        .info(
            "operation=\"getUserSubscriptions\" userId=\"1234\" loggerState=\"success\" activeSubscription=\"S-12345\" outcome=\"success\"");
    verifyNoMoreInteractions(mockLogger);
  }

  @Test
  public void debug_capabilities() {
    FluentLogger operation = operation("getUserSubscriptions", mockLogger);
    operation.with("userId", "1234").started();
    operation.logDebug(
        "The user has a lot of subscriptions", Collections.singletonMap("subscriptionCount", 999));

    operation.with("activeSubscription", "S-12345");
    operation.wasSuccessful();

    verify(mockLogger, times(2)).isInfoEnabled();
    verify(mockLogger, times(1)).isDebugEnabled();
    verify(mockLogger)
        .info("operation=\"getUserSubscriptions\" userId=\"1234\" loggerState=\"started\"");
    verify(mockLogger)
        .debug(
            "operation=\"getUserSubscriptions\" userId=\"1234\" loggerState=\"started\""
                + " debugMessage=\"The user has a lot of subscriptions\" subscriptionCount=999");
    verify(mockLogger)
        .info(
            "operation=\"getUserSubscriptions\" userId=\"1234\" loggerState=\"success\" activeSubscription=\"S-12345\" outcome=\"success\"");
    verifyNoMoreInteractions(mockLogger);
  }

  @Test
  public void debug_when_finished_operation() {
    FluentLogger operation = operation("getUserSubscriptions", mockLogger).started();
    operation.with("userId", "1234").wasSuccessful();
    operation.logDebug(
        "The user has a lot of subscriptions", Collections.singletonMap("subscriptionCount", 999));
    verify(mockLogger)
        .debug(
            "operation=\"getUserSubscriptions\" loggerState=\"success\" userId=\"1234\""
                + " debugMessage=\"The user has a lot of subscriptions\" subscriptionCount=999");
  }

  @Test
  public void create_action_with_no_operation() {
    action("compound_action", mockLogger).started().wasSuccessful();

    verify(mockLogger, times(2)).isInfoEnabled();
    verify(mockLogger).info("action=\"compound_action\" loggerState=\"started\"");
    verify(mockLogger)
        .info("action=\"compound_action\" loggerState=\"success\" outcome=\"success\"");
    verifyNoMoreInteractions(mockLogger);
  }

  @Test
  public void create_action_with_operation() {
    FluentLogger operation = operation("compound_operation", mockLogger).started();
    doAction();
    operation.wasSuccessful();

    verify(mockLogger, times(4)).isInfoEnabled();
    verify(mockLogger).info("operation=\"compound_operation\" loggerState=\"started\"");
    verify(mockLogger)
        .info(
            "operation=\"compound_operation\" action=\"compound_action\" loggerState=\"started\"");
    verify(mockLogger)
        .info(
            "operation=\"compound_operation\" action=\"compound_action\" loggerState=\"success\" outcome=\"success\"");
    verify(mockLogger)
        .info("operation=\"compound_operation\" loggerState=\"success\" outcome=\"success\"");

    verifyNoMoreInteractions(mockLogger);
  }

  private void doAction() {
    action("compound_action", mockLogger).started().wasSuccessful();
  }

  @Test
  public void finish_not_started_operation() {
    FluentLogger operation = operation("compound_operation", mockLogger);
    doAction();
    operation.wasSuccessful();

    verify(mockLogger, times(3)).isInfoEnabled();
    verify(mockLogger)
        .info(
            "operation=\"compound_operation\" action=\"compound_action\" loggerState=\"started\"");
    verify(mockLogger)
        .info(
            "operation=\"compound_operation\" action=\"compound_action\" loggerState=\"success\" outcome=\"success\"");
    verify(mockLogger)
        .info("operation=\"compound_operation\" loggerState=\"success\" outcome=\"success\"");

    verifyNoMoreInteractions(mockLogger);
  }

  @Test
  public void not_finished_operation() {
    try (FluentLogger operation = operation("compound_operation", mockLogger).started()) {
      doAction();
    }

    verify(mockLogger, times(3)).isInfoEnabled();
    verify(mockLogger, times(1)).isErrorEnabled();

    verify(mockLogger).info("operation=\"compound_operation\" loggerState=\"started\"");
    verify(mockLogger)
        .info(
            "operation=\"compound_operation\" action=\"compound_action\" loggerState=\"started\"");
    verify(mockLogger)
        .info(
            "operation=\"compound_operation\" action=\"compound_action\" loggerState=\"success\" outcome=\"success\"");
    verify(mockLogger)
        .error(
            "operation=\"compound_operation\" loggerState=\"fail\" "
                + "result=\"Programmer error: operation auto-closed before wasSuccessful() or wasFailure() called.\" "
                + "outcome=\"failure\"");

    verifyNoMoreInteractions(mockLogger);
  }

  @Test
  public void as_layout() {
    SimpleFluentLogger.changeDefaultLayout(Layout.Json);
    operation("simple_op", mockLogger).as(Layout.KeyValuePair).started().wasSuccessful();

    verify(mockLogger, times(2)).isInfoEnabled();
    verify(mockLogger).info("operation=\"simple_op\" loggerState=\"started\"");
  }

  @Test
  public void simple_json_layout() {
    operation("compound_success", mockLogger).asJson().started().wasSuccessful();

    verify(mockLogger, times(2)).isInfoEnabled();

    ArgumentCaptor<String> lines = ArgumentCaptor.forClass(String.class);
    verify(mockLogger, times(2)).info(lines.capture());

    final String line1 = lines.getAllValues().get(0);
    assertTrue(line1 + " must contain logLevel", line1.contains("\"logLevel\":\"INFO\""));
    assertTrue(
        line1 + " must contain operation", line1.contains("\"operation\":\"compound_success\""));
    assertTrue(line1 + " must contain loggerState", line1.contains("\"loggerState\":\"started\""));
    assertTrue(line1 + " must not contain outcome", !line1.contains("\"outcome\":\"success\""));

    final String line2 = lines.getAllValues().get(1);
    assertTrue(line2 + " must contain logLevel", line2.contains("\"logLevel\":\"INFO\""));
    assertTrue(
        line2 + " must contain operation", line2.contains("\"operation\":\"compound_success\""));
    assertTrue(line2 + " must contain loggerState", line2.contains("\"loggerState\":\"success\""));
    assertTrue(line2 + " must contain outcome", line2.contains("\"outcome\":\"success\""));

    verifyNoMoreInteractions(mockLogger);
  }

  @Test
  public void change_default_layout_to_json() {
    SimpleFluentLogger.changeDefaultLayout(Layout.Json);
    operation("simple_op", mockLogger).started().wasSuccessful();

    verify(mockLogger, times(2)).isInfoEnabled();

    ArgumentCaptor<String> lines = ArgumentCaptor.forClass(String.class);
    verify(mockLogger, times(2)).info(lines.capture());

    final String line1 = lines.getAllValues().get(0);
    assertTrue(line1 + " must contain logLevel", line1.contains("\"logLevel\":\"INFO\""));
  }

  @Test
  public void change_layout_to_key_value_for_operation() {
    SimpleFluentLogger.changeDefaultLayout(Layout.Json);
    operation("simple_op", mockLogger).asKeyValuePairs().started().wasSuccessful();

    verify(mockLogger, times(2)).isInfoEnabled();
    verify(mockLogger).info("operation=\"simple_op\" loggerState=\"started\"");
  }

  @Test
  public void validate_valid_key() {
    operation("simple_op", mockLogger).validate(KeyRegex.CamelCase).started().wasSuccessful();
  }

  @Test
  public void validate_valid_parameters() {
    SimpleFluentLogger.changeDefaultKeyRegex(KeyRegex.CamelCase);
    operation("simple_op", mockLogger)
        .started()
        .with("activeSubscription", "S-12345")
        .wasSuccessful();
  }

  @Test(expected = AssertionError.class)
  public void validate_invalid_key() {
    SimpleFluentLogger.changeDefaultKeyRegex(KeyRegex.CamelCase);
    operation("simple_op", mockLogger).started().with("InvalidKey", "1").wasSuccessful();
  }

  @Test
  public void disable_default_key_validation() {
    SimpleFluentLogger.disableDefaultKeyValidation();
    operation("simple_op", mockLogger).started().with("InvalidKey", "1").wasSuccessful();
  }

  @Test
  public void disable_key_validation_for_operation() {
    operation("simple_op", mockLogger)
        .disableKeyValidation()
        .started()
        .with("InvalidKey", "1")
        .wasSuccessful();
  }

  @Test(expected = AssertionError.class)
  public void validate_per_operation() {
    disableDefaultKeyValidation();

    operation("simple_op", mockLogger)
        .validate(KeyRegex.CamelCase)
        .started()
        .with("InvalidKey", "1")
        .wasSuccessful();
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

    verify(mockLogger).warn("operation=\"compound_success\" loggerState=\"started\"");
    verify(mockLogger)
        .warn("operation=\"compound_success\" loggerState=\"success\" outcome=\"success\"");
    verifyNoMoreInteractions(mockLogger);
  }

  @Test
  public void at_different_level() {
    operation("compound_success", mockLogger).started().at(Level.WARN).wasSuccessful();

    verify(mockLogger, times(1)).isInfoEnabled();
    verify(mockLogger, times(1)).isWarnEnabled();

    verify(mockLogger).info("operation=\"compound_success\" loggerState=\"started\"");
    verify(mockLogger)
        .warn("operation=\"compound_success\" loggerState=\"success\" outcome=\"success\"");
    verifyNoMoreInteractions(mockLogger);
  }

  @Test
  public void at_same_level() {
    operation("compound_success", mockLogger).started().at(Level.INFO).wasSuccessful();

    verify(mockLogger, times(2)).isInfoEnabled();

    verify(mockLogger).info("operation=\"compound_success\" loggerState=\"started\"");
    verify(mockLogger)
        .info("operation=\"compound_success\" loggerState=\"success\" outcome=\"success\"");
    verifyNoMoreInteractions(mockLogger);
  }

  @Test
  public void at_disabled_level() {
    Mockito.when(mockLogger.isTraceEnabled()).thenReturn(false);

    operation("compound_success", mockLogger).started().at(Level.TRACE).wasSuccessful();

    verify(mockLogger, times(1)).isInfoEnabled();
    verify(mockLogger, times(1)).isTraceEnabled();

    verify(mockLogger).info("operation=\"compound_success\" loggerState=\"started\"");
    verifyNoMoreInteractions(mockLogger);
  }

  @Test
  public void change_default_level() {
    SimpleFluentLogger.changeDefaultLevel(Level.WARN);
    operation("compound_success", mockLogger).started().wasSuccessful();

    verify(mockLogger, times(2)).isWarnEnabled();

    verify(mockLogger).warn("operation=\"compound_success\" loggerState=\"started\"");
    verify(mockLogger)
        .warn("operation=\"compound_success\" loggerState=\"success\" outcome=\"success\"");
    verifyNoMoreInteractions(mockLogger);
  }

  @Test
  public void to_string() {
    final String result = operation("compound_success", mockLogger).started().toString();

    assertEquals("name=compound_success type=operation state=startedState", result);
  }

  @Test(expected = EmptyValueException.class)
  public void empty_operation_name() {
    operation("", mockLogger).started();
    verifyNoMoreInteractions(mockLogger);
  }

  @Test(expected = EmptyValueException.class)
  public void null_operation_name() {
    operation(null, mockLogger).started();
    verifyNoMoreInteractions(mockLogger);
  }
}
