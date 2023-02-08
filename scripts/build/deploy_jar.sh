#!/bin/bash

# Initialize
START_LOCATION=$(pwd)
. $(dirname "$0")/init.sh

# Includes
. $SCRIPT_DIR/libs/version.sh
. $SCRIPT_DIR/libs/utils.sh

echo [INFO] This will publish jars on the kendar maven repo. Ctrl+C to exit
echo [INFO] Target version: $HAM_VERSION

pause

# Extra initializations
ROOT_DIR=$( cd -- "$( dirname -- "$SCRIPT_DIR" )" &> /dev/null && pwd )

# Deploys all jars on kendar mvn
cd $ROOT_DIR/ham
echo [INFO] Deploying ham
mvn deploy -DskipTests
mvn clean

# Restore previous dir
cd $START_LOCATION