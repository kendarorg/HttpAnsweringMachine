#!/bin/bash

# Initialize
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd $SCRIPT_DIR

HAM_VERSION=4.1.5
# Retrieve the jar name

AGENT_PATH=$SCRIPT_DIR/api.test/org.jacoco.agent-0.8.8-runtime.jar
EXEC_PATH=$SCRIPT_DIR/api.test/target/test_run_starter.exec



# Start the application
java "-Dloader.path=$SCRIPT_DIR/libs"  -Dloader.main=org.kendar.Main  \
	  	"-Djsonconfig=$SCRIPT_DIR/test.external.json" -Dham.tempdb=data/tmp \
	  	"-javaagent:$AGENT_PATH=destfile=$EXEC_PATH,includes=org.kendar.**" \
		  -jar "$SCRIPT_DIR/app/target/app-$HAM_VERSION.jar" org.springframework.boot.loader.PropertiesLauncher &

sleep 15

mvn test
