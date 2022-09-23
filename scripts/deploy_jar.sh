#!/bin/sh

# Initialize
START_LOCATION=$(pwd)
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

# Includes
. $SCRIPT_DIR/libs/version.sh
. $SCRIPT_DIR/libs/utils.sh

echo This will publish jars on the kendar maven repo. Ctrl+C to exit
echo Target version: $HAM_VERSION

pause

# Extra initializations
ROOT_DIR=$( cd -- "$( dirname -- "$SCRIPT_DIR" )" &> /dev/null && pwd )

# Deploys all jars on kendar mvn
cd $ROOT_DIR/ham
echo Deploying ham
mvn deploy
mvn clean

# Restore previous dir
cd $START_LOCATION