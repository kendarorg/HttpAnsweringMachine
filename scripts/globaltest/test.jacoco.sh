#!/bin/bash

# Initialize
START_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd $START_DIR
cd ..
cd ..
ROOT_DIR_MAIN=$(pwd)
cd ham
SCRIPT_DIR=$(pwd)


HAM_VERSION=4.3.0
# Retrieve the jar name
cd jacoco
mvn test
mv target/coverage-report $ROOT_DIR_MAIN/release/
