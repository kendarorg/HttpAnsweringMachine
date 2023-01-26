Feature: Testing db recorder
  user should be able to record replay db and so on
  Background:
    Given I have a server listening on port '9091'
    And I have a running HAM instance
    And I add a db proxy from 'testDb' to localH2Databse

  Scenario: Recording and storing simulated
    Given user create a recording 'TestDb'
    And user set parameter 'DB_RECORD_CALLS' to 'true'
    And user set parameter 'DB_USE_SIMULATED_ENGINE' to 'true'

    And user execute update query on 'testDb' with 'DROP TABLE IF EXISTS TEST'
    And user start recording
    And user execute update query on 'testDb' with 'CREATE TABLE TEST(ID BIGINT NOT NULL PRIMARY KEY, NAME VARCHAR(255))'
    And user execute update query on 'testDb' with 'INSERT INTO TEST VALUES (1,\'TestName\')'
    And user execute query on 'testDb' with 'SELECT * FROM TEST'
    And should find '1' record
    And user stop recording
    Then user can download as 'testdbs.json'
    And user delete recording
    And file 'testdbs.json' does not contains 'testDb/Connection/connect'
    And file 'testdbs.json' does not contains 'setAutoCommit'



  Scenario: Recording and storing normal
    Given user create a recording 'TestDb'
    And user set parameter 'DB_RECORD_CALLS' to 'true'
    And user execute update query on 'testDb' with 'DROP TABLE IF EXISTS TEST'
    And user start recording
    And user execute update query on 'testDb' with 'CREATE TABLE TEST(ID BIGINT NOT NULL PRIMARY KEY, NAME VARCHAR(255))'
    And user execute update query on 'testDb' with 'INSERT INTO TEST VALUES (1,\'TestName\')'
    And user execute query on 'testDb' with 'SELECT * FROM TEST'
    And should find '1' record
    And user stop recording
    Then user can download as 'testdbn.json'
    And user delete recording
    And file 'testdbn.json' contains 'testDb/Connection/connect'
    And file 'testdbn.json' does not contains 'setAutoCommit'

  Scenario: Recording and storing with void
    Given user create a recording 'TestDb'
    And user set parameter 'DB_RECORD_CALLS' to 'true'
    And user set parameter 'DB_RECORD_VOID_CALLS' to 'true'
    And user execute update query on 'testDb' with 'DROP TABLE IF EXISTS TEST'
    And user start recording
    And user execute update query on 'testDb' with 'CREATE TABLE TEST(ID BIGINT NOT NULL PRIMARY KEY, NAME VARCHAR(255))'
    And user execute update query on 'testDb' with 'INSERT INTO TEST VALUES (1,\'TestName\')'
    And user execute query on 'testDb' with 'SELECT * FROM TEST'
    And should find '1' record
    And user stop recording
    Then user can download as 'testdbv.json'
    And user delete recording
    And file 'testdbv.json' contains 'setAutoCommit'
    And file 'testdbv.json' contains 'testDb/Connection/connect'