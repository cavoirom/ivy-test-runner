package com.cavoirom.ivy.test.runner;

import ch.ivyteam.ivy.environment.EnvironmentNotAvailableException;
import ch.ivyteam.ivy.environment.Ivy;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.junit.Ignore;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sortable;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.spockframework.runtime.Sputnik;
import spock.lang.Specification;

public class IvyTestRunner extends Runner implements Filterable, Sortable {
  private Class<?> testClass;
  private TestResult testResult;
  private BlockJUnit4ClassRunner junitRunner;
  private Sputnik spockRunner;

  public IvyTestRunner(Class<?> testClass) throws InitializationError {
    this.testClass = testClass;
    if (!isIvyEnvironment()) {
      try {
        testResult = IvyTestConnector.newInstance().sendTestRequest(testClass);
      } catch (Exception ex) {
        throw new InitializationError(ex);
      }
    }
    if (isSpecification(testClass)) {
      this.spockRunner = new Sputnik(testClass);
    } else {
      this.junitRunner = new BlockJUnit4ClassRunner(testClass);
    }
  }

  private Optional<TestCaseResult> getTestCaseResult(Description description) {
    return testResult.getCases().stream()
        .filter(
            caseResult ->
                StringUtils.equalsIgnoreCase(caseResult.getName(), description.getMethodName()))
        .findFirst();
  }

  private boolean wasIgnored(FrameworkMethod method) {
    // TODO use this method for ignored test case
    return method.getAnnotation(Ignore.class) != null;
  }

  private boolean wasSuccessful(TestCaseResult testCaseResult) {
    return StringUtils.equalsIgnoreCase(testCaseResult.getStatus(), TestCaseResult.SUCCESSFUL);
  }

  private boolean wasFailure(TestCaseResult testCaseResult) {
    return StringUtils.equalsIgnoreCase(testCaseResult.getStatus(), TestCaseResult.FAILURE);
  }

  private boolean isIvyEnvironment() {
    try {
      Ivy.log(); // Verify Ivy Environment
      return true;
    } catch (EnvironmentNotAvailableException ex) {
      return false;
    }
  }

  private boolean isSpecification(Class<?> testClass) {
    Class<?> specClass = testClass.getSuperclass();
    while (specClass != null) {
      if (specClass == Specification.class) {
        return true;
      }
      specClass = specClass.getSuperclass();
    }
    return false;
  }

  @Override
  public void sort(Sorter sorter) {
    if (isSpecification(testClass)) {
      spockRunner.sort(sorter);
    } else {
      junitRunner.sort(sorter);
    }
  }

  @Override
  public void filter(Filter filter) throws NoTestsRemainException {
    if (isSpecification(testClass)) {
      spockRunner.filter(filter);
    } else {
      junitRunner.filter(filter);
    }
  }

  @Override
  public Description getDescription() {
    if (isSpecification(testClass)) {
      return spockRunner.getDescription();
    } else {
      return junitRunner.getDescription();
    }
  }

  @Override
  public void run(RunNotifier notifier) {
    if (isIvyEnvironment() && isSpecification(testClass)) {
      spockRunner.run(notifier);
    } else if (isIvyEnvironment()) {
      junitRunner.run(notifier);
    } else {
      getDescription()
          .getChildren()
          .forEach(
              child ->
                  getTestCaseResult(child)
                      .ifPresent(
                          testCaseResult -> {
                            if (wasSuccessful(testCaseResult)) {
                              notifier.fireTestStarted(child);
                              notifier.fireTestFinished(
                                  Description.createTestDescription(
                                      testClass, child.getMethodName()));
                            } else if (wasFailure(testCaseResult)) {
                              notifier.fireTestStarted(child);
                              notifier.fireTestFailure(
                                  new Failure(child, new Throwable(testCaseResult.getTrace())));
                              notifier.fireTestFinished(
                                  Description.createTestDescription(
                                      testClass, child.getMethodName()));
                            } else {
                              notifier.fireTestStarted(child);
                              notifier.fireTestFailure(
                                  new Failure(
                                      child,
                                      new Throwable("The test case could not be run on server.")));
                              notifier.fireTestFinished(
                                  Description.createTestDescription(
                                      testClass, child.getMethodName()));
                            }
                          }));
    }
  }
}
