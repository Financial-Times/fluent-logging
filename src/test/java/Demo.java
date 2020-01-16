import static com.ft.membership.logging.SimpleFluentLogger.action;
import static com.ft.membership.logging.SimpleFluentLogger.operation;

import com.ft.membership.logging.FluentLogger;
import java.util.UUID;

public class Demo {

  public static void main(String[] args) {
    new Demo().run();
  }

  @SuppressWarnings("divzero")
  protected void run() {
    final FluentLogger operation =
        operation("operation", this).with("id", UUID.randomUUID()).started();

    try {
      final Result result = getResult();
      operation.wasSuccessful(result);
    } catch (Exception e) {
      operation.wasFailure(e);
    }
  }

  private Result getResult() {
    final FluentLogger action = action("getResult", this).started();

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
