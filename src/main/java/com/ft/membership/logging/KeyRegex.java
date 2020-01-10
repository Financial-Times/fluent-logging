package com.ft.membership.logging;

public enum KeyRegex {
  CamelCase("([a-z]+[A-Z]*\\w+)+");

  private final String regex;

  KeyRegex(String regex) {
    this.regex = regex;
  }

  public String getRegex() {
    return regex;
  }
}
