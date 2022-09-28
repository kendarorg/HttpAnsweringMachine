#!/bin/sh

# Initialize
START_LOCATION=$(pwd)
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

# Includes
. $SCRIPT_DIR/libs/version.sh
. $SCRIPT_DIR/libs/utils.sh

echo This will build the docker images for the samples
echo and publish them on local docker. Ctrl+C to exit
echo Target version: $HAM_VERSION

pause

# Extra initializations
ROOT_DIR=$( cd -- "$( dirname -- "$SCRIPT_DIR" )" &> /dev/null && pwd )

echo NOT YET IMPLEMENTED
pause