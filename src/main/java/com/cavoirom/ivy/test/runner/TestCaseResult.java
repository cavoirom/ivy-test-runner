package com.cavoirom.ivy.test.runner;

public class TestCaseResult {
  public static final String SUCCESSFUL = "Successful";
  public static final String FAILURE = "Failure";

  private String className;
  private String name;
  private String status;
  private String trace;

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getTrace() {
    return trace;
  }

  public void setTrace(String trace) {
    this.trace = trace;
  }
}
