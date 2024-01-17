Feature: HttpRecording
  Capture an http website without applying settings (http://eu.httpbin.org/get)

  Background:
    Given Cache initialized
    And Ham started

  Scenario: Setup proxies
    When Set recording 'httprecording.setup_proxies'
    And Tell proxy to intercept all http calls