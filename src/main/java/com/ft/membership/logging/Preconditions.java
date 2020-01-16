package com.ft.membership.logging;

public class Preconditions {

  public static class EmptyValueException extends RuntimeException {
    public EmptyValueException() {
      super();
    }

    public EmptyValueException(String s) {
      super(s);
    }
  }

  public static String checkIsEmpty(String string, Object errorMessage) {
    if (string == null || string.isEmpty()) {
      throw new EmptyValueException(String.valueOf(errorMessage));
    }
    return string;
  }

  public static <T> T checkNotNull(T reference, Object errorMessage) {
    if (reference == null) {
      throw new NullPointerException(String.valueOf(errorMessage));
    }
    return reference;
  }

  public static <T> T checkNotNull(T reference) {
    if (reference == null) {
      throw new NullPointerException();
    }
    return reference;
  }
}
