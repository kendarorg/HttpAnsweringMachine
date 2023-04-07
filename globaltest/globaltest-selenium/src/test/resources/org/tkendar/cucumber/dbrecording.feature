Feature: DbRecording
  Prepare a 3 layer application, run it, record it and test with null infrastructure

  Background:
    Given Cache initialized
    And Ham started
    And Selenium initialized
    And Start h2 db
    And Start applications 'gateway,fe'
    And Initialized calendar rewrite url and H2 Db
    And Start applications 'be'
    And Adding dns for 'www.sample.test'
    And Adding dns for 'gateway.sample.test'
    And Adding dns for 'be.sample.test'
    And Adding ssl for '*.sample.test'
    And Adding ssl for 'sample.test'
    And Wait for 'be' to be ready calling 'http://127.0.0.1:8100/api/v1/health' for '120' seconds
    And Stop applications 'be'
    And Start calendar recording 'Main'
    And Start applications 'benogen'
    And Wait for 'benogen' to be ready calling 'http://127.0.0.1:8100/api/v1/health' for '120' seconds
    And Navigate calendar ui
    And Stop recording 'Main'

  Scenario: Run the ui test
    Given Initialized calendar rewrite url and H2 Db
    And The page does not contains 'Bing_logo'
    And Wait '3' seconds
    When Adding ssl for 'www.google.com'
    And Adding dns for 'www.google.com'
    And Creating filter to change google to bing
    And Resetting driver
    Then The google home page
    And The page contains 'Bing_logo'
    And Wait '3' seconds
    And Quit selenium
