package com.ft.membership.logging;

import com.ft.membership.common.types.userid.UserId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import static com.ft.membership.logging.DomainObjectKey.ErightsGroupId;
import static com.ft.membership.logging.DomainObjectKey.ErightsId;
import static com.ft.membership.logging.Operation.operation;
import static com.ft.membership.logging.Operation.resultOperation;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class OperationTest {

    @Mock
    Logger mockLogger;

    @Before
    public void setup() {
        Mockito.when(mockLogger.isInfoEnabled()).thenReturn(true);
        Mockito.when(mockLogger.isErrorEnabled()).thenReturn(true);
    }
    
    @Test
    public void should_log_simple_success() throws Exception {

        operation("simple_success").started(this).wasSuccessful().log(mockLogger);

        verify(mockLogger).info("operation=\"simple_success\" outcome=\"success\"");
    }

    @Test
    public void should_log_simple_success_for_result_operation() throws Exception {

        resultOperation("simple_result_operation").started(this).wasSuccessful().log(mockLogger);

        verify(mockLogger).info(Mockito.matches("operation=\"simple_result_operation\" outcome=\"success\" timeToComplete=\"\\d*\""));
    }

    @Test
    public void should_log_with_time_to_complete() throws Exception {

        operation("simple_with_time").withTimeToComplete().started(this).wasSuccessful().log(mockLogger);

        verify(mockLogger).info(Mockito.matches("operation=\"simple_with_time\" outcome=\"success\" timeToComplete=\"\\d*\""));
    }

    @Test
    public void should_log_success_for_user_id() throws Exception {

        final UserId userId = UserId.randomUserId();
        operation("simple_success").with(userId).started(this).wasSuccessful().log(mockLogger);

        verify(mockLogger).info(
            eq("operation=\"simple_success\" outcome=\"success\" userId=\"" + userId + "\"")
        );
    }

    @Test
    public void should_log_success_for_user_id_and_other_params() throws Exception {

        final UserId userId = UserId.randomUserId();
        operation("simple_success")
                .with(userId)
                .with("x", 100)
                .with("y", "that quick brown fox")
                .started(this)
                .wasSuccessful()
                .log(mockLogger);

        verify(mockLogger).info(
                eq("operation=\"simple_success\" outcome=\"success\" userId=\"" + userId + "\" x=100 y=\"that quick brown fox\"")
        );
    }


    @Test
    public void should_log_success_for_user_id_and_key_params() throws Exception {

        final UserId userId = UserId.randomUserId();
        operation("simple_success")
                .with(userId)
                .with(ErightsId,1000)
                .with("y", "that quick brown fox")
                .started(this)
                .wasSuccessful()
                .log(mockLogger);

        verify(mockLogger).info(
            eq("operation=\"simple_success\" outcome=\"success\" userId=\"" + userId + "\" erightsId=1000 y=\"that quick brown fox\"")
        );
    }

    @Test
    public void should_log_success_for_user_id_and_key_params_yielding() throws Exception {

        final UserId userId = UserId.randomUserId();
        operation("simple_success")
                .with(ErightsId, 1000)
                .with("y", "that quick brown fox")
                .started(this)
                .wasSuccessful()
                .yielding(userId)
                .yielding(ErightsGroupId, 100)
                .log(mockLogger);

        verify(mockLogger).info(
            eq("operation=\"simple_success\" outcome=\"success\" erightsId=1000 y=\"that quick brown fox\" userId=\"" + userId + "\" erightsGroupId=100")
        );
    }


    @Test
    public void should_log_simple_failure() throws Exception {

        operation("simple_failure").started(this).wasFailure().withMessage("boo hoo").log(mockLogger);

        verify(mockLogger).error(
                eq("operation=\"simple_failure\" outcome=\"failure\" errorMessage=\"boo hoo\"")
                );
    }

    @Test
    public void should_log_failure_with_exception() throws Exception {


        final Exception ex = new RuntimeException("bang!");
        operation("simple_failure").started(this).wasFailure().throwingException(ex).log(mockLogger);

        verify(mockLogger).error(
                eq("operation=\"simple_failure\" outcome=\"failure\" errorMessage=\"bang!\" exception=\"java.lang.RuntimeException: bang!\""),
                eq(ex)
        );
    }

    @Test
    public void should_log_failure_with_parameters_and_exception() throws Exception {

        final Exception ex = new RuntimeException("bang!");
        operation("simple_failure")
                .with("x", 101)
                .with("y","bat")
                .started(this)
                .wasFailure()
                .throwingException(ex)
                .withMessage("got a puncture")
                .withDetail("tyre","right")
                .log(mockLogger);

        verify(mockLogger).error(
                eq("operation=\"simple_failure\" outcome=\"failure\" errorMessage=\"got a puncture\" x=101 y=\"bat\" tyre=\"right\" exception=\"java.lang.RuntimeException: bang!\""),
                eq(ex)
        );
    }

    @Test
    public void should_allow_null_values_for_parameters_and_yields() throws Exception {
        operation("allow_nulls")
                .with("nullableInput", null)
                .started(this)
                .wasSuccessful()
                .yielding("nullableResult",null)
                .log(mockLogger);

        verify(mockLogger).info(
                eq("operation=\"allow_nulls\" outcome=\"success\" nullableInput=null nullableResult=null")
        );

    }

    @Test(expected = NullPointerException.class)
    public void should_not_allow_null_values_for_operation_keys() throws Exception {
        operation("no_null_operation_keys")
                .with((String) null, "aValue");

    }

    @Test(expected = NullPointerException.class)
    public void should_not_allow_null_values_for_yield_keys() throws Exception {
        operation("no_null_yield_keys").started(this)
                .wasSuccessful().yielding((String) null, "aValue");

    }

    @Test(expected = NullPointerException.class)
    public void should_not_allow_null_value_for_exception() throws Exception {
        operation("no_null_yield_keys").started(this)
                .wasFailure().throwingException(null).log(mockLogger);

    }

    @Test
    public void should_log_error_if_used_in_try_with_resources_and_not_terminated() throws Exception {

        try(Operation operation = operation("try-with-resources").with("a", 5).started(mockLogger)) {
            // do nothing
        }

        verify(mockLogger).error(
                eq("operation=\"try-with-resources\" outcome=\"failure\" errorMessage=\"operation auto-closed\" a=5 exception=\"java.lang.RuntimeException: operation auto-closed\""),
                any(RuntimeException.class)
        );
    }

    @Test
    public void should_log_normally_if_used_in_try_with_resources_and_properly_terminated() throws Exception {

        try(Operation operation = operation("try-with-resources").with("a", 5).started(mockLogger)) {
            operation.wasSuccessful().log(mockLogger);
        }

        verify(mockLogger).info(
                eq("operation=\"try-with-resources\" outcome=\"success\" a=5")
        );
    }

}
