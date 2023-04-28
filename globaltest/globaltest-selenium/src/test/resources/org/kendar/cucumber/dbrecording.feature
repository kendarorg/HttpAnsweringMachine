Feature: DbRecording
  Prepare a 3 layer application, run it, record it and test with null infrastructure

  Background:
    Given Cache initialized
    And Stop applications 'fe,app,be,gateway,bemongo'
    And Ham started
    And Selenium initialized

  Scenario: Setup proxies
    When Set recording 'dbrecording.setup_proxies'
    And Initialized calendar rewrite url and H2 Db
    And Adding dns for 'www.sample.test'
    And Adding dns for 'gateway.sample.test'
    And Adding dns for 'be.sample.test'
    And Adding ssl for '*.sample.test'
    And Adding ssl for 'sample.test'
    And Quit selenium

  Scenario: Record interaction
    When Set recording 'dbrecording.record_interaction'
    Given Start h2 db
    And Prepare HAM setup
    And Start applications 'gateway,fe'
    And Wait '5' seconds
    # Start the backend waiting for the db to be completely started
    And Start applications 'bedbham'
    # Wait for be with db initialization to be ready
    And Wait for 'bedbham' to be ready calling 'http://127.0.0.1:8100/api/v1/health' for '120' seconds
    # Stop be
    And Stop applications 'be'
    When Start calendar recording 'Main'
    # Start the be with an already initialized database
    # Does not create the db and tables, just use them
    And Start applications 'benogen'
    And Wait for 'benogen' to be ready calling 'http://127.0.0.1:8100/api/v1/health' for '120' seconds
    # Navigate the application
    Then Navigate calendar ui
    And Stop action 'Main'
    And Download recording 'Main'
    And Stop applications 'be,gateway,fe,app'
    And Quit selenium

  Scenario: Run the ui test
    When Set recording 'dbrecording.run_ui_test'
    Given Start applications 'fe'
    And Prepare HAM setup
    And Wait for 'fe' to be ready calling 'http://127.0.0.1:8080/api/v1/health' for '120' seconds
    And Upload recording 'Main'
    And Clone recording 'Main' into 'UiTest'
    # Remove the calls not relative to the ui (fe, be, db)
    And Prepare UI test 'UiTest'
    # Play, just respond to any matching request
    Then Start replaying 'UiTest'
    # Navigate a ui without any gateway
    And Navigate calendar ui
    And Stop action 'UiTest'
    And Download recording 'UiTest'
    And Stop applications 'fe,app'
    And Quit selenium

  Scenario: Run the null gateway test
    When Set recording 'dbrecording.run_null_gateway'
    Given Start applications 'gateway'
    And Prepare HAM setup
    And Wait for 'gateway' to be ready calling 'http://127.0.0.1:8090/api/v1/health' for '120' seconds
    And Upload recording 'Main'
    And Clone recording 'Main' into 'NullGatewayTest'
    # Remove the calls not relative to the ui (fe, db)
    And Prepare NullGateway test 'NullGatewayTest'
    # To verify the schema of the requests
    And Set NullGateway verification script for 'NullGatewayTest'
    # Automatic test of the gateway
    When Start null playing 'NullGatewayTest'
    And Wait for termination
    # Expect successful result
    Then Load results 'true' for 'NullGatewayTest'
    And Download recording 'NullGatewayTest'
    And Stop applications 'gateway,app'
    And Quit selenium

  Scenario: Run the null gateway failed test
    When Set recording 'dbrecording.run_null_gateway_failed'
    Given Start applications 'gateway'
    And Prepare HAM setup
    And Wait for 'gateway' to be ready calling 'http://127.0.0.1:8090/api/v1/health' for '120' seconds
    And Upload recording 'Main'
    And Clone recording 'Main' into 'NullGatewayFailTest'
    # Remove the calls not relative to the ui (fe, db)
    And Prepare NullGateway test 'NullGatewayFailTest'
    # To verify the schema of the requests
    And Set NullGateway verification script for 'NullGatewayFailTest'
    # Change a payload to force an error
    And Set NullGateway verification fail for 'NullGatewayFailTest'
    # Automatic test of the failing gateway
    When Start null playing 'NullGatewayFailTest'
    And Wait for termination
    # Expect failed result
    Then Load results 'false' for 'NullGatewayFailTest'
    And Download recording 'NullGatewayFailTest'
    And Stop applications 'gateway,app'
    And Quit selenium

  Scenario: Run the be fake db test
    When Set recording 'dbrecording.fake_h2_test'
    Given Prepare HAM setup
    And Upload recording 'Main'
    And Clone recording 'Main' into 'DbNullTest'
    And Prepare db null test 'DbNullTest'
    # To allow the initialisation of an existing database
    Then Start replaying 'DbNullTest'
    # Does not create the db and tables, just use them
    Given Start applications 'benogen'
    And Wait for 'benogen' to be ready calling 'http://127.0.0.1:8100/api/v1/health' for '120' seconds
    # Now the db is initialized
    And Stop action 'DbNullTest'
    # Automatic test of the failing gateway
    When Start null playing 'DbNullTest'
    And Wait for termination
    # Expect successful result
    Then Load results 'true' for 'DbNullTest'
    And Download recording 'DbNullTest'
    And Stop applications 'be,app'
    And Quit selenium
