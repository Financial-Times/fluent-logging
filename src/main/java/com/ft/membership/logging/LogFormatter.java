package com.ft.membership.logging;

import static com.ft.membership.logging.LogFormatter.NameAndValue.nameAndValue;
import static com.ft.membership.logging.Preconditions.checkNotNull;
import static org.slf4j.event.Level.TRACE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

public class LogFormatter {
  private static final String OUTCOME_IS_SUCCESS = "success";
  private static final String OUTCOME_IS_FAILURE = "failure";
  private static final String LOG_LEVEL = "logLevel";
  private static final String TIME = "time";
  private static final String INFO = "INFO";
  private static final String DEBUG = "DEBUG";
  private static final String ERROR = "ERROR";
  private static final String WARN = "WARN";
  private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSz";
  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern(DATE_PATTERN);

  private final Logger logger;
  private final ObjectWriter objectWriter;

  LogFormatter(Object actorOrLogger) {
    objectWriter = new ObjectMapper().writer();
    checkNotNull(actorOrLogger, "require actor or logger");
    if (actorOrLogger instanceof Logger) {
      logger = (Logger) actorOrLogger;
    } else {
      logger = LoggerFactory.getLogger(actorOrLogger.getClass());
    }
  }

  void log(
      final OperationContext operation,
      final Outcome outcome,
      final Level logLevel,
      final Layout layout) {
    final Collection<NameAndValue> msgParams = new ArrayList<>();

    addOperationParameters(operation, msgParams);

    if (outcome != null) {
      addOutcome(outcome.getKey(), msgParams);
    }

    switch (logLevel) {
      case DEBUG:
        if (logger.isDebugEnabled()) {
          logger.debug(buildMsg(msgParams, DEBUG, layout));
        }
        break;
      case ERROR:
        if (logger.isErrorEnabled()) {
          logger.error(buildMsg(msgParams, ERROR, layout));
        }
        break;
      case WARN:
        if (logger.isWarnEnabled()) {
          logger.warn(buildMsg(msgParams, WARN, layout));
        }
        break;
      case TRACE:
        if (logger.isTraceEnabled()) {
          logger.trace(buildMsg(msgParams, TRACE.toString(), layout));
        }
        break;
      default:
        if (logger.isInfoEnabled()) {
          logger.info(buildMsg(msgParams, INFO, layout));
        }
    }
  }

  private String buildMsg(
      final Collection<NameAndValue> msgParams, String logLevel, Layout layout) {
    return layout == Layout.Json ? buildMsgJson(msgParams, logLevel) : buildMsgString(msgParams);
  }

  void logStart(final Operation operation) {
    final Collection<NameAndValue> msgParams = new ArrayList<NameAndValue>();
    addOperation(operation, msgParams);
    addOperationParameters(operation, msgParams);
    if (logger.isInfoEnabled()) {
      logger.info(buildMsg(operation, msgParams, INFO));
    }
  }

  void logInfo(final Operation operation, Yield yield, boolean terminateOperation) {
    if (terminateOperation) {
      operation.terminated();
    }

    if (logger.isInfoEnabled()) {
      final Collection<NameAndValue> msgParams = new ArrayList<NameAndValue>();
      addOperation(operation, msgParams);
      if (terminateOperation) {
        addOutcome(OUTCOME_IS_SUCCESS, msgParams);
      }
      addOperationParameters(operation, msgParams);
      addYield(yield, msgParams);
      logger.info(buildMsg(operation, msgParams, INFO));
    }
  }

  private String buildMsg(
      Operation operation, final Collection<NameAndValue> msgParams, String logLevel) {
    return operation.isJsonLayout() ? buildMsgJson(msgParams, logLevel) : buildMsgString(msgParams);
  }

  void logDebug(Operation operation, Yield yield, boolean terminateOperation) {
    if (terminateOperation) {
      operation.terminated();
    }

    if (logger.isDebugEnabled()) {
      final Collection<NameAndValue> msgParams = new ArrayList<NameAndValue>();
      addOperation(operation, msgParams);
      if (terminateOperation) {
        addOutcome(OUTCOME_IS_SUCCESS, msgParams);
      }
      addOperationParameters(operation, msgParams);
      addYield(yield, msgParams);
      logger.debug(buildMsg(operation, msgParams, DEBUG));
    }
  }

  void logInfo(Operation operation, Failure failure, boolean terminateOperation) {
    if (terminateOperation) {
      operation.terminated();
    }

    if (logger.isInfoEnabled()) {
      String failureMessage = buildFailureMessage(operation, failure, INFO);
      logger.info(failureMessage);
    }
  }

  void logWarn(Operation operation, Failure failure, boolean terminateOperation) {
    if (terminateOperation) {
      operation.terminated();
    }

    if (logger.isWarnEnabled()) {
      String failureMessage = buildFailureMessage(operation, failure, WARN);
      logger.warn(failureMessage);
    }
  }

  void logWarn(Operation operation, Yield yield, boolean terminateOperation) {
    if (terminateOperation) {
      operation.terminated();
    }

    if (logger.isWarnEnabled()) {
      final Collection<NameAndValue> msgParams = new ArrayList<NameAndValue>();
      addOperation(operation, msgParams);
      addYield(yield, msgParams);

      logger.warn(buildMsg(operation, msgParams, WARN));
    }
  }

  void logError(final Operation operation, Failure failure, boolean terminateOperation) {
    if (terminateOperation) {
      operation.terminated();
    }

    if (logger.isErrorEnabled()) {
      String failureMessage = buildFailureMessage(operation, failure, ERROR);

      if (failure.didThrow()) {
        logger.error(failureMessage, failure.getThrown());
      } else {
        logger.error(failureMessage);
      }
    }
  }

  void logError(Operation operation, Yield yield, boolean terminateOperation) {
    if (terminateOperation) {
      operation.terminated();
    }

    if (logger.isErrorEnabled()) {
      final Collection<NameAndValue> msgParams = new ArrayList<>();
      addOperation(operation, msgParams);
      addYield(yield, msgParams);

      logger.error(buildMsg(operation, msgParams, ERROR));
    }
  }

  private String buildFailureMessage(final Operation operation, Failure failure, String logLevel) {
    final Collection<NameAndValue> msgParams = new ArrayList<NameAndValue>();
    addOperation(operation, msgParams);
    addOutcome(OUTCOME_IS_FAILURE, msgParams);
    addFailureMessage(failure, msgParams);
    addOperationParameters(operation, msgParams);
    addFailureDetails(failure, msgParams);
    return buildMsg(operation, msgParams, logLevel);
  }

  private String buildMsgString(final Collection<NameAndValue> msgParams) {
    return msgParams.stream().map(param -> param.toString()).collect(Collectors.joining(" "));
  }

  private String buildMsgJson(final Collection<NameAndValue> msgParams, String logLevel) {
    addLogLevalAndTime(msgParams, logLevel);
    Map<String, Object> map = new HashMap<>();
    msgParams.stream()
        .forEach(
            nameAndValue -> {
              map.put(nameAndValue.getName(), nameAndValue.getValue());
            });

    String jsonResult = "";
    try {
      jsonResult = objectWriter.writeValueAsString(map).concat(System.lineSeparator());
    } catch (JsonProcessingException e) {
      logger.info("Failed to serialize the object to JSON, error={}", e.getLocalizedMessage());
    }
    return jsonResult;
  }

  private void addOperation(final Operation operation, final Collection<NameAndValue> msgParams) {
    msgParams.add(nameAndValue("operation", operation.getName()));
  }

  private void addOperationParameters(
      final Operation operation, final Collection<NameAndValue> msgParams) {
    addParametersAsNamedValues(msgParams, operation.getParameters());
  }

  private void addOperationParameters(
      final OperationContext operation, final Collection<NameAndValue> msgParams) {
    addParametersAsNamedValues(msgParams, operation.getParameters());
  }

  private void addOutcome(String outcome, final Collection<NameAndValue> msgParams) {
    msgParams.add(nameAndValue("outcome", outcome));
  }

  private void addOutcome(Outcome outcome, final Collection<NameAndValue> msgParams) {
    msgParams.add(nameAndValue("outcome", outcome));
  }

  private void addYield(Yield yield, final Collection<NameAndValue> msgParams) {
    addParametersAsNamedValues(msgParams, yield.getParameters());
  }

  private void addParametersAsNamedValues(
      final Collection<NameAndValue> msgParams, Map<String, Object> parameters) {
    for (String name : parameters.keySet()) {
      msgParams.add(nameAndValue(name, parameters.get(name)));
    }
  }

  private void addFailureMessage(Failure failure, final Collection<NameAndValue> msgParams) {
    msgParams.add(nameAndValue("errorMessage", failure.getFailureMessage()));
  }

  private void addFailureDetails(Failure failure, final Collection<NameAndValue> msgParams) {
    addParametersAsNamedValues(msgParams, failure.getParameters());

    if (failure.didThrow()) {
      msgParams.add(nameAndValue("exception", failure.getThrown().toString()));
    }
  }

  private void addLogLevalAndTime(final Collection<NameAndValue> msgParams, String logLevel) {
    msgParams.add(nameAndValue(LOG_LEVEL, logLevel));
    msgParams.add(
        nameAndValue(
            TIME, java.time.ZonedDateTime.now(ZoneOffset.UTC).format(DATE_TIME_FORMATTER)));
  }

  static class NameAndValue {
    private String name;
    private Object value;

    static NameAndValue nameAndValue(String name, Object value) {
      return new NameAndValue(name, value);
    }

    private NameAndValue(String name, Object value) {
      this.name = name;
      this.value = value;
    }

    public String toString() {
      if (value instanceof Integer || value instanceof Long) {
        return String.format("%s=%d", name, value);
      } else {
        return String.format("%s=%s", name, new ToStringWrapper(value).toString());
      }
    }

    public String getName() {
      return name;
    }

    public Object getValue() {
      return value;
    }
  }
}
