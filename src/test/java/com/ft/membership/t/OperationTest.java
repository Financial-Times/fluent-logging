package com.ft.membership.t;

import com.ft.membership.logging.Key;
import com.ft.membership.logging.Operation;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.ft.membership.logging.Operation.operation;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OperationTest {

    @Mock
    Logger mockLogger;

    @Before
    public void setup() {
        Mockito.when(mockLogger.isInfoEnabled()).thenReturn(true);
        Mockito.when(mockLogger.isErrorEnabled()).thenReturn(true);
        Mockito.when(mockLogger.isDebugEnabled()).thenReturn(true);
    }
    
    @Test
    public void log_start_and_success() throws Exception {

        operation("simple_success").started(mockLogger).wasSuccessful().log();

        verify(mockLogger,times(2)).isInfoEnabled();
        verify(mockLogger).info("operation=\"simple_success\"");
        verify(mockLogger).info("operation=\"simple_success\" outcome=\"success\"");
        verifyNoMoreInteractions(mockLogger);
    }

  @Test
  public void log_start_and_success_for_json_layout() throws Exception {

    operation("simple_success").jsonLayout().started(mockLogger).wasSuccessful().log();

    verify(mockLogger, times(2)).isInfoEnabled();
  }

  @Test
  public void only_log_success_for_initiated_operation() throws Exception {

    operation("simple_result_operation").initiate(mockLogger).wasSuccessful().log();

    verify(mockLogger, times(1)).isInfoEnabled();
    verify(mockLogger).info("operation=\"simple_result_operation\" outcome=\"success\"");
    verifyNoMoreInteractions(mockLogger);
  }

  @Test
  public void only_log_success_for_initiated_operation_for_json_layout() throws Exception {

    operation("simple_result_operation").jsonLayout().initiate(mockLogger).wasSuccessful().log();

    verify(mockLogger, times(1)).isInfoEnabled();
  }

    @Test
    public void log_success_with_start_params() throws Exception {

        final UUID userId = UUID.randomUUID();
        final Map<String, Object> mapOfParams  = new HashMap<>();
        mapOfParams.put("beta", "b");
        mapOfParams.put("zeta", "z");

        operation("simple_success")
                .with(Key.UserId, userId)
                .with("y", "that quick brown fox")
                .with(mapOfParams)
                .started(mockLogger)
                .wasSuccessful()
                .log();

        verify(mockLogger).info(
                eq("operation=\"simple_success\" userId=\"" + userId + "\" y=\"that quick brown fox\" zeta=\"z\" beta=\"b\"")
        );

        verify(mockLogger).info(
            eq("operation=\"simple_success\" outcome=\"success\" userId=\"" + userId + "\" y=\"that quick brown fox\" zeta=\"z\" beta=\"b\"")
        );
    }

    @Test
    public void log_success_with_key_yield() throws Exception {

        String email = "user@test.com";
        operation("simple_success")
                .with("y", "that quick brown fox")
                .started(mockLogger)
                .wasSuccessful()
                .yielding(Key.UserEmail, email)
                .log();

        verify(mockLogger).info(
                eq("operation=\"simple_success\" y=\"that quick brown fox\"")
        );

        verify(mockLogger).info(
            eq("operation=\"simple_success\" outcome=\"success\" y=\"that quick brown fox\" email=\"" + email + "\"")
        );
    }

    @Test
    public void log_success_with_kv_yield() throws Exception {

        String email = "user@test.com";

        Map<String, Object> mapOfValues = new HashMap<>();
        mapOfValues.put("kappa", "k");
        mapOfValues.put("delta", "d");

        operation("simple_success")
                .with("y", "that quick brown fox")
                .started(mockLogger)
                .wasSuccessful()
                .yielding("userEmail", email)
                .yielding(mapOfValues)
                .log();

        verify(mockLogger).info(
                eq("operation=\"simple_success\" y=\"that quick brown fox\"")
        );

        verify(mockLogger).info(
                eq("operation=\"simple_success\" outcome=\"success\" y=\"that quick brown fox\" userEmail=\"" + email + "\" delta=\"d\" kappa=\"k\"")
        );
    }

    @Test
    public void log_simple_failure() throws Exception {

        operation("simple_failure").started(mockLogger).wasFailure().withMessage("boo hoo").log();

        verify(mockLogger).error(
                eq("operation=\"simple_failure\" outcome=\"failure\" errorMessage=\"boo hoo\"")
                );
    }

  @Test
  public void log_simple_failure_for_json_layout() throws Exception {

    operation("simple_failure").jsonLayout()
        .started(mockLogger)
        .wasFailure()
        .withMessage("boo hoo")
        .log();

    verify(mockLogger).isErrorEnabled();
  }

    @Test
    public void log_failure_with_exception() throws Exception {

        final Exception ex = new RuntimeException("bang!");
        operation("simple_failure").started(mockLogger).wasFailure().throwingException(ex).log();
        verify(mockLogger).isErrorEnabled();
    }

    @Test
    public void log_failure_with_exception_for_json_layout() throws Exception {

        final Exception ex = new RuntimeException("bang!");
        operation("simple_failure").jsonLayout().started(mockLogger).wasFailure().throwingException(ex).log();

        verify(mockLogger).isErrorEnabled();
    }

    @Test
    public void log_failure_with_quiet_exception() throws Exception {

        final Exception ex = new RuntimeException("bang!");
        operation("simple_failure").started(mockLogger).wasFailure().withMessage(ex).log();

        verify(mockLogger).error(
                eq("operation=\"simple_failure\" outcome=\"failure\" errorMessage=\"bang!\"")
        );
    }

    @Test
    public void log_failure_with_parameters_and_exception() throws Exception {

        final Exception ex = new RuntimeException("bang!");
        operation("simple_failure")
                .with("x", 101)
                .with("y","bat")
                .started(mockLogger)
                .wasFailure()
                .throwingException(ex)
                .withMessage("got a puncture")
                .withDetail("tyre","right")
                .log();

        verify(mockLogger).error(
                eq("operation=\"simple_failure\" outcome=\"failure\" errorMessage=\"got a puncture\" x=101 y=\"bat\" tyre=\"right\" exception=\"java.lang.RuntimeException: bang!\""),
                eq(ex)
        );
    }

    @Test
    public void log_failure_with_parameters_and_exception_for_json_layout() throws Exception {

        final Exception ex = new RuntimeException("bang!");
        operation("simple_failure").jsonLayout()
                .with("x", 101)
                .with("y","bat")
                .started(mockLogger)
                .wasFailure()
                .throwingException(ex)
                .withMessage("got a puncture")
                .withDetail("tyre","right")
                .log();

        verify(mockLogger,times(1)).isErrorEnabled();
    }

    @Test
    public void allow_null_values_for_parameters_and_yields() throws Exception {
        operation("allow_nulls")
                .with("nullableInput", null)
                .started(mockLogger)
                .wasSuccessful()
                .yielding("nullableResult",null)
                .log();

        verify(mockLogger).info(
                eq("operation=\"allow_nulls\" outcome=\"success\" nullableInput=null nullableResult=null")
        );

    }

    @Test
    public void allow_null_values_for_parameters_and_yields_for_json_layout() throws Exception {
        operation("allow_nulls").jsonLayout()
                .with("nullableInput", null)
                .started(mockLogger)
                .wasSuccessful()
                .yielding("nullableResult",null)
                .log();

        verify(mockLogger, times(2)).isInfoEnabled();
    }

    @Test(expected = NullPointerException.class)
    public void not_allow_null_values_for_operation_keys() throws Exception {
        operation("no_null_operation_keys")
                .with((String) null, "aValue");

    }

    @Test(expected = NullPointerException.class)
    public void not_allow_null_values_for_yield_keys() throws Exception {
        operation("no_null_yield_keys").started(mockLogger)
                .wasSuccessful().yielding((String) null, "aValue");

    }

    @Test(expected = NullPointerException.class)
    public void not_allow_null_value_for_exception() throws Exception {
        operation("no_null_yield_keys").started(mockLogger)
                .wasFailure().throwingException(null).log();

    }

    @Test
    public void log_error_if_used_in_try_with_resources_and_not_terminated() throws Exception {

        try(Operation ignored = operation("try-with-resources").with("a", 5).started(mockLogger)) {
            // do nothing
        }

        verify(mockLogger).error(
                eq("operation=\"try-with-resources\" outcome=\"failure\" errorMessage=\"Programmer error: operation auto-closed before wasSuccessful() or wasFailure() called.\" a=5 exception=\"java.lang.IllegalStateException: Programmer error: operation auto-closed before wasSuccessful() or wasFailure() called.\""),
                any(RuntimeException.class)
        );
    }

    @Test
    public void log_normally_if_used_in_try_with_resources_and_properly_terminated() throws Exception {

        try(Operation operation = operation("try-with-resources").with("a", 5).started(mockLogger)) {
            operation.wasSuccessful().log();
        }

        verify(mockLogger).info(
                eq("operation=\"try-with-resources\" outcome=\"success\" a=5")
        );
    }

    @Test
    @Ignore("For documentation only")
    public void documentation() throws Exception {
        class LaunchException extends RuntimeException {String reason() {return "crash";} String malfunction() {return "parachute";}}
        class Flight {String position() {return "[10.0,17.2,0.0]";}}
        class Probe {Flight launch() {if(System.currentTimeMillis() %2 == 0) throw new LaunchException(); else return new Flight();} public String toString() {return "27";}}
        Probe probe = new Probe();
        String target = "Mars";

        Operation operation = Operation.operation("launch")
                .with("probe", probe)
                .with("target", target)
                .started(this);

        try {
            Flight f = probe.launch();

            operation.wasSuccessful()
                    .yielding("position", f.position())
                    .log();

        } catch (LaunchException e) {
            operation.wasFailure()
                    .throwingException(e)
                    .withMessage(e.reason())
                    .withDetail("malfunction", e.malfunction())
                    .log();
        }


    }
}
