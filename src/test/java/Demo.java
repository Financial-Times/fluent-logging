import com.ft.membership.logging.Operation;

import java.util.UUID;

import static com.ft.membership.logging.Operation.operation;

public class Demo {

    public static void main(String[] args) {
        new Demo().run();
    }

    protected void run() {
        // report starting conditions
        final Operation operation = operation("operation").with("argument", UUID.randomUUID()).started(this);
        //result operation does not print out the starting conditions, only success/failures

        final Operation resultOperation = operation("resultOperation").with("argument", UUID.randomUUID()).initiate(this);

        //operation with JSON layout
        final Operation operationJson = Operation.operation("Operation that outputs in JSON format").jsonLayout()
                .with("argument", UUID.randomUUID()).started(this);

        try {
            // do some things
            int x = 1/0;
            // report success
            operation.wasSuccessful().yielding("result","{\"text\": \"hello world\"}").log();
            resultOperation.wasSuccessful().yielding("result","{\"text\": \"hello world\"}").log();

            operationJson.wasSuccessful().yielding("result","{\"text\": \"hello world\"}").log();

        } catch(Exception e) {
            // report failure
            operation.wasFailure().throwingException(e).log();
            resultOperation.wasFailure().throwingException(e).log();
            operationJson.wasFailure().throwingException(e).log();
        }
    }
}
