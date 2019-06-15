package com.cavoirom.ivy.test.runner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.runners.model.InitializationError;

public class IvyTestConnector {
  private static final String TEST_SERVER_URL =
      "http://{host}:{port}/ivy/pro/{application}"
          + "/ivy-test-server/16B53B3E6F6E3E12/start.ivp?testClassPath={testClassPath}&moduleName={moduleName}";
  private static final String TEST_EXECUTOR_PAGE_URL = "http://{host}:{port}{path}";
  private static final Pattern MAVEN_PROJECT_PATH_PATTERN =
      Pattern.compile(".*/(.*)/target/test-classes/");
  private static final Pattern JAVA_PROJECT_PATH_PATTERN = Pattern.compile(".*/(.*)/classes/");
  private static final Pattern TEST_RESULT_JSON_PATTERN =
      Pattern.compile("<body>(.*)</body>", Pattern.DOTALL);

  private IvyTestConnector() {}

  public static IvyTestConnector newInstance() {
    return new IvyTestConnector();
  }

  public TestResult sendTestRequest(Class<?> testClass) throws IOException, InitializationError {
    String hostname = "localhost";
    String port = "8081";
    String application = isDesigner() ? "designer" : "Portal";
    String moduleName = parseModuleName(testClass);
    // Connect to Test Server start url
    String testServerUrl = buildTestServerUrl(hostname, port, application, testClass, moduleName);
    System.out.println("[INFO] Open: " + testServerUrl);
    HttpURLConnection startConnection = (HttpURLConnection) new URL(testServerUrl).openConnection();
    startConnection.setInstanceFollowRedirects(false);
    startConnection.setRequestMethod("GET");
    // Follow the redirect of start url to Test Executor Page
    String cookie = parseCookie(startConnection.getHeaderField("Set-Cookie"));
    String testExecutorPagePath = startConnection.getHeaderField("Location");
    String testExecutorPageUrl = buildTestExecutorPageUrl(hostname, port, testExecutorPagePath);
    System.out.println("[INFO] Redirect: " + testExecutorPageUrl);
    HttpURLConnection executeConnection =
        (HttpURLConnection) new URL(testExecutorPageUrl).openConnection();
    executeConnection.setRequestMethod("GET");
    executeConnection.setRequestProperty("Cookie", cookie);
    // Get the HTML content
    String response =
        IOUtils.toString(
            new BufferedReader(new InputStreamReader(executeConnection.getInputStream())));
    // Parse content to TestResult
    return parseTestResult(response);
  }

  private TestResult parseTestResult(String response) throws InitializationError {
    Matcher matcher = TEST_RESULT_JSON_PATTERN.matcher(response);
    if (matcher.find()) {
      String json = StringEscapeUtils.unescapeHtml4(matcher.group(1)).trim();
      System.out.println("[INFO] Result: " + json);
      return TestResult.fromJson(json);
    } else {
      throw new InitializationError("Could not parse test result return from server: " + response);
    }
  }

  private String buildTestExecutorPageUrl(
      String hostname, String port, String testExecutorPagePath) {
    return TEST_EXECUTOR_PAGE_URL
        .replace("{host}", hostname)
        .replace("{port}", port)
        .replace("{path}", testExecutorPagePath);
  }

  private String buildTestServerUrl(
      String hostname, String port, String application, Class<?> testClass, String moduleName) {
    return TEST_SERVER_URL
        .replace("{host}", hostname)
        .replace("{port}", port)
        .replace("{application}", application)
        .replace("{testClassPath}", testClass.getCanonicalName())
        .replace("{moduleName}", moduleName);
  }

  private String parseCookie(String rawCookie) {
    return rawCookie.substring(0, rawCookie.indexOf(";"));
  }

  private String parseModuleName(Class<?> testClass) {
    String classPath = testClass.getClassLoader().getResource(".").getPath();

    Matcher mavenProjectMatcher = MAVEN_PROJECT_PATH_PATTERN.matcher(classPath);
    Matcher javaProjectMatcher = JAVA_PROJECT_PATH_PATTERN.matcher(classPath);

    if (mavenProjectMatcher.find()) {
      return mavenProjectMatcher.group(1);
    } else if (javaProjectMatcher.find()) {
      return javaProjectMatcher.group(1);
    }
    return StringUtils.EMPTY;
  }

  private boolean isDesigner() {
    try {
      // This class only exists on Ivy Designer (from version 7)
      Class.forName("com.ulcjava.container.local.server.LocalContainerAdapter");
      return true;
    } catch (ClassNotFoundException ex) {
      return false;
    }
  }
}
