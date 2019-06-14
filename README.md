# Ivy Test Runner
Running Unit Test with Ivy Environment

#### Background
In 2017, when I was working at Axon Active Vietnam, there was a need to run Unit Test within Ivy Environment because we don't want to Mock Ivy Environment, the code of mocking Ivy Environment behaviors are some time larger than the actual test code and maintaining those mocks are not easy. That motivates me writing this test runner. The first version was done when I was working at Axon Active Vietnam. Now I rewrite it base on original idea and adding some improvements.

#### Features
* Run JUnit4 Tests and Spock Specifications in Ivy Environment (Axon.ivy Engine)

##

#### Setup

##### pom.xml

##### Running from Ivy Designer
* Right click on test case and Run as Unit Test

##### Running by Maven

```mvn clean verify```
