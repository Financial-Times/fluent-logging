package com.ft.membership.logging;

import java.util.Objects;

public class ToStringWrapper {
  private final Object value;

  public static ToStringWrapper wrap(final Object value) {
    return new ToStringWrapper(value);
  }

  public ToStringWrapper(final Object value) {
    this.value = value;
  }

  @Override
  public String toString() {
    if (value == null) return "null";
    return "\"" + value.toString().replace("\"", "\\\"") + "\"";
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof String) {
      return this.toString().equals(obj);
    } else if (obj instanceof ToStringWrapper) {
      if (value == null) return ((ToStringWrapper) obj).value == null;
      return this.value.equals(((ToStringWrapper) obj).value);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }
}
