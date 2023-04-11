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
    And Start applications 'gateway,fe'
    And Wait '5' seconds
    When Start calendar recording 'MainMongo'
    # Start the backend waiting for the db to be completely started
    And Start applications 'bemongo'
    And Wait for 'bemongo' to be ready calling 'http://127.0.0.1:8100/api/v1/health' for '60' seconds
    # Navigate the application
    Then Navigate calendar ui
    And Stop action 'MainMongo'
    And Download recording 'MainMongo'
    And Stop applications 'be,bemongo,gateway,fe,app'
    And Quit selenium
    And Stop mongodb

  Scenario: Run the be fake mongo test
    Given Prepare HAM setup
    And Prepare mongo proxy
    And Upload recording 'Main'
    And Clone recording 'Main' into 'DbMongoNullTest'
    And Prepare db null test 'DbMongoNullTest'
    # To allow the initialisation of an existing database
    Then Start replaying 'DbMongoNullTest'
    # Does not create the db and tables, just use them
    Given Start applications 'benogen'
    And Wait for 'benogen' to be ready calling 'http://127.0.0.1:8100/api/v1/health' for '120' seconds
    # Now the db is initialized
    And Stop action 'DbMongoNullTest'
    # Automatic test of the failing gateway
    When Start null playing 'DbMongoNullTest'
    And Wait for termination
    # Expect successful result
    Then Load results 'true' for 'DbMongoNullTest'
    And Download recording 'DbMongoNullTest'
    And Stop applications 'be,app'
    And Quit selenium