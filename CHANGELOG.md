#4.0.1
* Add new `.with(Object key, Object value)` method to `FluentLogger` that sets the key with `key.toString()`

#4.0.0
* Deprecate Operation, Failure, Yield, IntermediateYield
* Introduce OperationContext and OperationState as abstract types for building fluent logs
* Provide sample implementation with SimpleOperationContext fit for the FT use case with operations and actions

#3.1.1

* Allow intermediate logs for a given Operation

# 3.1.0

* Add methods to add all key-values from a map to parameters and yields.
* Check the log level before formatting messages. 
* Improved error message when auto-closed before succeeding or failing.
* No longer has a dependency on any version of Google Guava.

# 3.0.0

* First public Maven release.
