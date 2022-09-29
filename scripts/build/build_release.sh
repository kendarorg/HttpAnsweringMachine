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
mvn clean install > "$ROOT_DIR"/release/ham-"$HAM_VERSION".log 2>1



# Copy result
echo Copying result to target
. $SCRIPT_DIR/build/libs/copy_ham.sh
. $SCRIPT_DIR/build/libs/copy_simpledns.sh
# Prepare the run commands

# Prepare the compressed file
echo Compress release file
cd $ROOT_DIR/release/$HAM_VERSION
tar -zcvf "$ROOT_DIR"/release/ham-"$HAM_VERSION".tar.gz . >> "$ROOT_DIR"/release/ham-"$HAM_VERSION".log 2>1

# Cleanup
echo Cleanup
rm -rf $HAM_RELEASE_TARGET || true

# Restore previous dir
cd $START_LOCATION

