## Basic structure

Intercept and record any HTTP/S request "wrapping" the Application Under Test
(AUT from now). Like a STATEFUL recording API gateway.

SCENARIOS_1

The flow can be then be run to simulate various situations

SCENARIOS_2

The content can be edited to anonymize data via the Web UI

Every recorded request/response can be modified at runtime via 
Javascript code

## Black box tests

Given a recording, it can be run to simulate full flows:

* HAM runs the recorded calls to the AUT
* The AUT calls HAM as if it were an external application
* HAM verifies the schema and content, producing the test results

## Expanding scenarios

Given a recording it can be modified and integrate with the web ui
to simulate various situations

* Slow response times
* Failed responses
* Situations not covered by the recording (aka wrong data or unexpected contents)

## Evolution scenarios

When new apis are provided they can be simulated via Javascript dynamic scripts

## Check 