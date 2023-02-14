#!/bin/bash

# Initialize
START_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd $START_DIR
cd ..
cd ..
cd ham
SCRIPT_DIR=$(pwd)


HAM_VERSION=4.1.5
# Retrieve the jar name
cd jacoco
mvn test