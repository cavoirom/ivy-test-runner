package com.cavoirom.ivy.test.runner;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;

public class TestResult {
  List<TestCaseResult> cases;

  public TestResult() {
    this.cases = new ArrayList<>();
  }

  public List<TestCaseResult> getCases() {
    return cases;
  }

  public void setCases(List<TestCaseResult> cases) {
    this.cases = cases;
  }

  public String toJson() {
    return new Gson().toJson(this);
  }

  public static TestResult fromJson(String jsonString) {
    return new Gson().fromJson(jsonString, TestResult.class);
  }
}
