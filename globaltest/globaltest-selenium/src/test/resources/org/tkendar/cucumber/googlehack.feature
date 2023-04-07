Feature: Google Hack Example
  Rewrite google homepage to show bing logo

  Background:
    Given Cache initialized
    And Ham started
    And Selenium initialized

  Scenario: Intercept google and change the logo
    Given The google home page
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
