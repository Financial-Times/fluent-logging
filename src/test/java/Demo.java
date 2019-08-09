import static com.ft.membership.logging.Operation.operation;

import com.ft.membership.logging.Operation;
import java.util.UUID;

public class Demo {

  public static void main(String[] args) {
    new Demo().run();
  }

  @SuppressWarnings("divzero")
  protected void run() {
    // report starting conditions
    final Operation operation = operation("operation").with("argument", UUID.randomUUID())
        .started(this);
    //result operation does not print out the starting conditions, only success/failures
    final Operation resultOperation = operation("resultOperation")
        .with("argument", UUID.randomUUID()).initiate(this);

    try {
      // do some things
      int x = 1 / 0;
      // report success
      operation.wasSuccessful().yielding("result", "{\"text\": \"hello world\"}").log();
      resultOperation.wasSuccessful().yielding("result", "{\"text\": \"hello world\"}").log();

    } catch (Exception e) {
      // report failure
      operation.wasFailure().throwingException(e).log();
      resultOperation.wasFailure().throwingException(e).log();
    }
  }
}
