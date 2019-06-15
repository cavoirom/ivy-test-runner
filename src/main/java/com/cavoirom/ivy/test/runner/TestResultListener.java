package com.cavoirom.ivy.test.runner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class TestResultListener extends RunListener {
  private static final String SEPARATOR = ":";
  private Map<String, TestCaseResult> testCaseResults;

  public TestResultListener() {
    testCaseResults = new HashMap<>();
  }

  public void testRunStarted(Description description) {}

  public void testRunFinished(Result result) {}

  public void testStarted(Description description) {
    TestCaseResult testCaseResult = new TestCaseResult();
    testCaseResult.setClassName(description.getClassName());
    testCaseResult.setName(description.getMethodName());
    testCaseResult.setStatus(TestCaseResult.SUCCESSFUL);
    testCaseResults.put(buildCaseKey(description), testCaseResult);
  }

  public void testFinished(Description description) {}

  public void testFailure(Failure failure) {
    TestCaseResult testCaseResult = testCaseResults.get(buildCaseKey(failure.getDescription()));
    testCaseResult.setStatus(TestCaseResult.FAILURE);
    testCaseResult.setTrace(failure.getTrace());
  }

  public void testIgnored(Description description) {}

  public TestResult getTestResult() {
    TestResult result = new TestResult();
    result.setCases(new ArrayList<>(testCaseResults.values()));
    return result;
  }

  private String buildCaseKey(Description description) {
    return description.getClassName() + SEPARATOR + description.getMethodName();
  }
}
