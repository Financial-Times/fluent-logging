import static com.ft.membership.logging.SimpleOperationContext.action;
import static com.ft.membership.logging.SimpleOperationContext.operation;

import com.ft.membership.logging.OperationContext;
import java.util.UUID;

public class Demo {

  public static void main(String[] args) {
    new Demo().run();
  }

  @SuppressWarnings("divzero")
  protected void run() {
    final OperationContext operation =
        operation("operation", this).with("id", UUID.randomUUID()).started();

    try {
      final Result result = getResult();
      operation.wasSuccessful(result);
    } catch (Exception e) {
      operation.wasFailure(e);
    }
  }

  private Result getResult() {
    final OperationContext action = action("getResult", this).started();

    final Result result = new Result();

    action.with("answer", result.answer).wasSuccessful(result);
    return result;
  }

  private class Result {

    public int answer = 42;

    @Override
    public String toString() {
      return "for tee two";
    }
  }
}
