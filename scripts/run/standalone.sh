#!/bin/sh

# Initialize
START_LOCATION=$(pwd)
. $(dirname "$0")/init.sh

# Includes
. $SCRIPT_DIR/libs/version.sh
. $SCRIPT_DIR/libs/utils.sh

echo This will publish jars on the kendar maven repo. Ctrl+C to exit
echo Target version: $HAM_VERSION

pause