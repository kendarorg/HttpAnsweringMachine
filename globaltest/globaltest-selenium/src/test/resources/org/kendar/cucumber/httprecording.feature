Feature: HttpRecording
  Capture an http website without applying settings (http://eu.httpbin.org/get)

  Background:
    Given Cache initialized
    And Ham started
    And Selenium initialized

  Scenario: Setup proxies
    When Set recording 'httprecording.setup_proxies'
    And Prepare HAM setup
    And Capture all http
    Then Start simple proxy recording 'AllHttp'
    And Call 'http' 'eu.httpbin.org' 'get'
    And Stop action 'AllHttp'
    And Download recording 'AllHttp'
    And The recording  'AllHttp' contains 'eu.httpbin.org'
    And Quit selenium