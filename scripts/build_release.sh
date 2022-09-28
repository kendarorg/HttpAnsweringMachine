#!/bin/sh

# Initialize
START_LOCATION=$(pwd)
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

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
mkdir -p $HAM_RELEASE_TARGET/ham
mkdir -p $HAM_RELEASE_TARGET/simpledns
mkdir -p $HAM_RELEASE_TARGET/ham/libs
mkdir -p $HAM_RELEASE_TARGET/ham/external

# Build HAM
cd $ROOT_DIR/ham
echo Building ham
mvn clean install > "$ROOT_DIR"/release/ham-"$HAM_VERSION".log 2>1



# Copy result
echo Copying result to target
cp "$ROOT_DIR"/ham/app/target/app-"$HAM_VERSION".jar "$HAM_RELEASE_TARGET"/ham/
cp "$ROOT_DIR"/ham/simpledns/target/simpledns-"$HAM_VERSION".jar "$HAM_RELEASE_TARGET"/simpledns/
cp "$ROOT_DIR"/ham/libs/*.jar "$HAM_RELEASE_TARGET"/ham/libs/
cp "$ROOT_DIR"/ham/external/*.* "$HAM_RELEASE_TARGET"/ham/external/
cp "$ROOT_DIR"/ham/external.json "$HAM_RELEASE_TARGET"/ham/

# Prepare the run commands
cp "$SCRIPT_DIR"/templates/releasebuild/ham/*.sh "$HAM_RELEASE_TARGET"/ham/
cp "$SCRIPT_DIR"/templates/releasebuild/simpledns/*.sh "$HAM_RELEASE_TARGET"/simpledns/
cp "$SCRIPT_DIR"/templates/releasebuild/ham/*.bat "$HAM_RELEASE_TARGET"/ham/
cp "$SCRIPT_DIR"/templates/releasebuild/simpledns/*.bat "$HAM_RELEASE_TARGET"/simpledns/

# Make executable
chmod +x "$HAM_RELEASE_TARGET"/ham/*.sh
chmod +x "$HAM_RELEASE_TARGET"/simpledns/*.sh

# Prepare the compressed file
echo Compress release file
cd $ROOT_DIR/release/$HAM_VERSION
tar -zcvf "$ROOT_DIR"/release/ham-"$HAM_VERSION".tar.gz . >> "$ROOT_DIR"/release/ham-"$HAM_VERSION".log 2>1

# Cleanup
echo Cleanup
rm -rf $HAM_RELEASE_TARGET || true

# Restore previous dir
cd $START_LOCATION

