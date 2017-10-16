Fluent Logging
==============

[![CircleCI](https://circleci.com/gh/Financial-Times/fluent-logging.svg?style=svg&circle-token=2bf1e9c418beb98c7445d741db96e04c54a577aa)](https://circleci.com/gh/Financial-Times/fluent-logging) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.ft.membership/fluent-logging/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.ft.membership/fluent-logging)

## Introduction
Fluent splunk-friendly logging with automatic escaping. Use this library to have a consistent logging format across your
libraries and applications.

## Using fluent-logging

Fluent splunk-friendly logging with automatic escaping; e.g

    public class Demo {

        public static void main(String[] args) {
            new Demo().run();
        }

        protected void run() {
            // report starting conditions
            final Operation operation = operation("operation").with("argument", UUID.randomUUID()).started(this);
            
            //operation with JSON layout
            final Operation operationJson = Operation.operation("Operation that outputs in JSON format").jsonLayout()
                        .with("argument", UUID.randomUUID()).started(this);

            try {
                // do some things
                int x = 1/0;
                // report success
                operation.wasSuccessful().yielding("result","{\"text\": \"hello world\"}").log();

            } catch(Exception e) {
                // report failure
                operation.wasFailure().throwingException(e).log();
            }
        }
    }

Refer [Demo.java](src/test/java/Demo.java) for full source code of an example.

Operation might log:

    09:42:28.503 [main] INFO  Demo - operation=demo id="bd09f108-2a4d-4e47-8b9f-d20c19c0dad0"
    09:42:28.543 [main] INFO  Demo - operation=demo id="bd09f108-2a4d-4e47-8b9f-d20c19c0dad0" outcome=success result="{\"text\": \"hello world\"}"
    11:54:19.199 [main] INFO Demo - {
      "argument" : "43a480ab-71a9-4bc7-83ea-be8f742f8cca",
      "operation" : "Operation that outputs in JSON format"
    }


on success, or:

    10:02:13.484 [main] ERROR Demo - operation=demo id="bd09f108-2a4d-4e47-8b9f-d20c19c0dad0" outcome=failure exception="java.lang.ArithmeticException: / by zero"
    java.lang.ArithmeticException: / by zero
        at Demo.run(Demo.java:19) [test-classes/:na]
        at Demo.main(Demo.java:10) [test-classes/:na]
        at ...
        
    12:19:08.168 [main] ERROR Demo - {
      "exception" : "java.lang.ArithmeticException: / by zero",
      "argument" : "280ac9ec-17f1-4470-ab5d-f748cac734dc",
      "errorMessage" : "/ by zero",
      "operation" : "Operation that outputs in JSON format",
      "outcome" : "failure"
    }

on failure.

The argument passed to the ```started()``` (and, optionally, terminating ```log()```) method is used to derive 
the logger name, and is usually the object which is the orchestrator of an operation. Alternatively, a specific `slf4j`
logger instance can be passed.

Arguments (passed  by ```with()``` and ```yielding()``` etc.) are escaped to allow Splunk to index them, e.g. double 
quotes are escaped.

See https://sites.google.com/a/ft.com/technology/systems/membership/logging-conventions for suggested argument key 
names.

## Fluent-Logging outputs in only KV-pairs

To remove plain text from logs and to have only KV pairs as output use following configuration of
the repository using Fluent-Logging library:

Dropwizard application:

        logFormat: "logLevel=\"%p\" time=\"%d{yyyy-MM-dd'T'HH:mm:ss.SSS}\" category=\"%c\" %m%n"

Spring Boot Application:

        logging.pattern.console=logLevel=\"%p\" time=\"%d{yyyy-MM-dd'T'HH:mm:ss.SSS}\" category=\"%c\" %m%n
        logging.pattern.file=logLevel=\"%p\" time=\"%d{yyyy-MM-dd'T'HH:mm:ss.SSS}\" category=\"%c\" %m%n

## Fluent-Logging outputs in JSON format

Create operation with jsonLayout:

        //operation with JSON layout
        final Operation operationJson = Operation.operation("Operation that outputs in JSON format").jsonLayout()
                        .with("argument", UUID.randomUUID()).started(this);

 