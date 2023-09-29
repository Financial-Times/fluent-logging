Fluent Logging
==============

[![CircleCI](https://circleci.com/gh/Financial-Times/fluent-logging.svg?style=svg&circle-token=2bf1e9c418beb98c7445d741db96e04c54a577aa)](https://circleci.com/gh/Financial-Times/fluent-logging) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.ft.membership/fluent-logging/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.ft.membership/fluent-logging)

## Introduction
fluent logging is java logging library having [splunk](https://www.splunk.com/) in mind. The goal of this library to provide a consistent logging format across
libraries and applications. `fluent-logging` is build on top of [slf4j](http://www.slf4j.org/) and the provided implementation utilises [MDC](http://www.slf4j.org/manual.html#mdc), so you would benefit if you use log4j or logback as logging backend.

## Basic Usage
You can use `SimpleFluentLogger` to create `operations` and `actions`. In this implementation during an `operation` you can create multiple `actions`. Each action is linked to the overarching `operation`.
Operations and actions follow the same basic lifecycle:
1. Call static factory method
1. Optionally configure any options valid for the whole operation/action (e.g. at(Level), as(Json), etc.)
1. Optionally attach known parameters using `with()`
1. Optionally call started if you want to have separate log for start
1. Optionally attach more parameters using `with()`
1. Either call wasSuccessful or wasFailure

- At any point after calling the static factory method you are free to call the logDebug method in order to log some specific message that will not attach any parameters to the original operation/action
```
import static com.ft.membership.logging.SimpleFluentLogger.operation;

import com.ft.membership.logging.FluentLogger;


public class Demo {

    public static void main(String[] args) {
        new Demo().run();
    }

    protected void run() {
        final FluentLogger operation = operation("name", this).with("argument", UUID.randomUUID()).started();            
        try {
            final Result result = ...;
            operation.wasSuccessful(result);
        } catch(Exception e) {
            operation.wasFailure(e);
        }
    }
}
```

## Example output
```
15:08:07.420 [main] INFO Demo - operation="operation" id="0781772d-0678-4b98-88a6-1042299fc69b" loggerState="started"
15:08:07.423 [main] INFO Demo - operation="operation" action="getResult" loggerState="started"
15:08:07.424 [main] INFO Demo - operation="operation" action="getResult" loggerState="success" answer=42 result="for tee two" outcome="success"
15:08:07.425 [main] INFO Demo - operation="operation" id="0781772d-0678-4b98-88a6-1042299fc69b" loggerState="success" result="for tee two" outcome="success"
```

## Additional usages
### JSON layout
By default the library uses key=value layout. You can use the JSON layout as well:
```
final FluentLogger operationJson = operation("name", this).asJson().with("argument", UUID.randomUUID()).started();
```
### Change default layout
```
SimpleFluentLogger.changeDefaultLayout(Layout.Json);
final FluentLogger operationJson = operation("name", this).with("argument", UUID.randomUUID()).started();
```
### Enable key validation
Field names in Splunk are case sensitive. You can enable camelCase validation for keys.
Be careful when using this feature since after enabling validation anytime you with("InvalidKey") a AssertionException will be thrown!
The goal of the exception is to take logs seriously and spot problems as soon as possible.
```
// Will throw an AssertionError because InvalidKey is not camel case
operation("simple_op", mockLogger).validate(KeyRegex.CamelCase).with("InvalidKey", "1").with("validKey", "2").started();
```

You can enable key validation globally:
```
SimpleFluentLogger.changeDefaultKeyRegex(KeyRegex.CamelCase);
operation("simple_op", mockLogger).with("InvalidKey", "1").with("validKey", "2").started();
```
### Log at specific level
You can log at different org.slf4j.event.Level
```
operation("compound_success", mockLogger).at(Level.WARN).started().wasSuccessful();
```

### More Info
Refer [SimpleFluentLoggerTest](src/test/java/com/ft/membership/t/SimpleFluentLoggerTest.java) to see all the capabilities of the provided implementation

### Actor
The second argument passed to the factory methods `operation` & `action` (called `actorOrLogger`) is used to derive the logger name,
and is usually the object which is the orchestrator of an operation.
Alternatively, a specific `slf4j` logger instance can be passed.

### Escaping
Arguments (for example attached `with()`) are escaped to allow Splunk to index them, e.g. double quotes are escaped.

## Advanced usage
You can create class similar to `SimpleFluentLogger` that inherits from `FluentLogger` and introduce different states by extending the `LoggerState` interface.
That way you can define your own transitions or advanced features.

## Logging Conventions
In FT Membership team we have a Confluence page defining the key/field names.
The recommendation is that all users of the library in specific domain should use the same key/field names.
You can create a document with conventions like us or share set of enums across projects.

## Fluent-Logging outputs in only KV-pairs
To remove plain text from logs and to have only KV pairs as output use following configuration of
the repository using Fluent-Logging library:

Dropwizard application:
```
logFormat: "logLevel=\"%p\" time=\"%d{yyyy-MM-dd'T'HH:mm:ss.SSSz}\" category=\"%c\" %m%n"
```

Spring Boot Application:
```
logging.pattern.console=logLevel=\"%p\" time=\"%d{yyyy-MM-dd'T'HH:mm:ss.SSSz}\" category=\"%c\" %m%n
logging.pattern.file=logLevel=\"%p\" time=\"%d{yyyy-MM-dd'T'HH:mm:ss.SSSz}\" category=\"%c\" %m%n
```

## Multi-threaded usage

As `SimpleFluentLogger` uses [MDC](http://www.slf4j.org/manual.html#mdc). Every thread by default have different context. This means that if you start an `operation`, spawn a thread and that thread logs an action it would not have an operation. In order to fix that the application should explicitly set the MDC of the new thread. See the following [Stack Overflow Post](https://stackoverflow.com/questions/6073019/how-to-use-mdc-with-thread-pools) for implementation suggestions.


## Contributing
- If you are within FT just talk to the Membership team. Otherwise please open issue in GitHub.
- This project uses [google java format](https://github.com/google/google-java-format)

## Internal Release
https://app.circleci.com/pipelines/github/Financial-Times/fluent-logging?branch=master 