Feature: MongoRecording
  Prepare a 3 layer application, run it, record it and test with null infrastructure with mongodb

  Background:
    Given Cache initialized
    And Stop applications 'fe,app,be,bemongo,gateway'
    And Ham started
    And Selenium initialized

  Scenario: Record interaction
    Given Start mongodb
    And Prepare HAM setup
    And Prepare mongo proxy
    And Start applications 'gateway,fe,bemongo'
    And Wait '10' seconds
    # And Wait for 'bemongo' to be ready calling 'http://127.0.0.1:8100/api/v1/health' for '60' seconds
    When Start calendar recording 'MainMongo'
    # Navigate the application
    Then Navigate calendar ui
    And Stop action 'MainMongo'
    And Download recording 'MainMongo'
    And Stop applications 'be,bemongo,gateway,fe,app'
    And Quit selenium
    And Stop mongodb

  Scenario: Run the ui test
    Given Start applications 'gateway,fe'
    Given Prepare HAM setup
    And Prepare mongo proxy
    And Wait for 'fe' to be ready calling 'http://127.0.0.1:8080/api/v1/health' for '120' seconds
    And Upload recording 'MainMongo'
    And Clone recording 'MainMongo' into 'DbMongoUiTest'
    # Remove the calls not relative to the ui (fe, be, db)
    And Prepare db only test 'DbMongoUiTest'
    # Play, just respond to any matching request
    Then Start replaying 'DbMongoUiTest'
    And Start applications 'bemongo'
    And Wait for 'bemongo' to be ready calling 'http://127.0.0.1:8100/api/v1/health' for '120' seconds
    # Navigate a ui without any gateway
    And Navigate calendar ui
    And Stop action 'DbMongoUiTest'
    And Download recording 'DbMongoUiTest'
    And Stop applications 'gateway,fe,bemongo,app'
    And Quit selenium

  Scenario: Run the be fake mongo test
    Given Prepare HAM setup
    And Prepare mongo proxy
    And Upload recording 'MainMongo'
    And Clone recording 'MainMongo' into 'DbMongoNullTest'
    And Prepare db null test 'DbMongoNullTest'
    And Set NullGateway verification script for 'DbMongoNullTest'
    # To allow the initialisation of an existing database
    Then Start replaying 'DbMongoNullTest'
    # Does not create the db and tables, just use them
    Given Start applications 'bemongo'
    And Wait for 'bemongo' to be ready calling 'http://127.0.0.1:8100/api/v1/health' for '60' seconds
    # Now the db is initialized
    And Stop action 'DbMongoNullTest'
    # Automatic test of the failing gateway
    When Start null playing 'DbMongoNullTest'
    And Wait for termination
    And Stop applications 'bemongo'
    # Expect successful result
    Then Load results 'true' for 'DbMongoNullTest'
    And Download recording 'DbMongoNullTest'
    And Stop applications 'be,bemongo,app'
    And Quit selenium