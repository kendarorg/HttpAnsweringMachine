#!/bin/sh

# Initialize
START_LOCATION=$(pwd)
. $(dirname "$0")/init.sh

# Includes
. $SCRIPT_DIR/libs/version.sh
. $SCRIPT_DIR/libs/utils.sh

echo This will build a tar.gz to run the application. Ctrl+C to exit
echo Target version: $HAM_VERSION

pause

# Extra initializations
ROOT_DIR=$( cd -- "$( dirname -- "$SCRIPT_DIR" )" &> /dev/null && pwd )

# Setup the target directory
echo Setup target dir
HAM_RELEASE_TARGET=$ROOT_DIR/release/$HAM_VERSION
rm -rf $HAM_RELEASE_TARGET || true
mkdir -p $HAM_RELEASE_TARGET

# Build HAM
cd $ROOT_DIR/ham
echo Building ham
mvn clean
rm -rf $ROOT_DIR/ham/jsplugins
rm -rf $ROOT_DIR/ham/libs
rm -rf $ROOT_DIR/ham/data
rm -rf $ROOT_DIR/ham/callogs
rm -rf $ROOT_DIR/ham/replayerdata

rm -rf $ROOT_DIR/jsplugins
rm -rf $ROOT_DIR/libs
rm -rf $ROOT_DIR/data
rm -rf $ROOT_DIR/callogs
rm -rf $ROOT_DIR/replayerdata

# Build samples
cd $ROOT_DIR/samples/calendar
echo Cleaning samples
call mvn clean

rm -rf $ROOT_DIR/release

REM Restore previous dir
cd %START_LOCATION%