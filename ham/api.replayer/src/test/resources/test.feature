Feature: Testing recorder
  user should be able to record replay stuffs and so on
  Background:
    Given I have a server listening on port '9091'
    And I have a running HAM instance
    And I add a dns mapping from '127.0.0.1' to 'gateway.int.test'
    And I add a dns mapping from '127.0.0.1' to 'www.nuget.org'
    And I add a proxy from 'http://gateway.int.test' to 'http://127.0.0.1:9091' testing it with '127.0.0.1:9091'

  Scenario: Recording and storing
    Given user create a recording 'Test'
    And user start recording 'Test'
    And user calls 'http://gateway.int.test/api/v2/$metadata'
    And user stop recording 'Test'
    Then user can download 'Test' as 'test.json'
    And file 'test.json' contains 'www.nuget.org'
    And file 'test.json' contains 'gateway.int.test'
    And user delete recording 'Test'

  Scenario: Replaying
    And I add a dns mapping from '127.0.0.1' to 'www.sticazzi.org'
    Given user upload 'test.json' as 'Test'
    And user start replaying 'Test'
    And user calls 'http://gateway.int.test/api/v2/$metadata'
    And the response should contain 'Edm.String'
    And user stop replaying 'Test'
    #
    #And user delete recording 'Test'

  #Scenario: PACT
  #Scenario: Null infrastructure