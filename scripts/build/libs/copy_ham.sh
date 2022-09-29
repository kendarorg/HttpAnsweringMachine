#!/bin/sh

echo Generate ham relase dirs
mkdir -p $HAM_RELEASE_TARGET/ham
mkdir -p $HAM_RELEASE_TARGET/ham/libs
mkdir -p $HAM_RELEASE_TARGET/ham/external

echo Copying ham to target
cp "$ROOT_DIR"/ham/app/target/app-"$HAM_VERSION".jar "$HAM_RELEASE_TARGET"/ham/
cp "$ROOT_DIR"/ham/libs/*.jar "$HAM_RELEASE_TARGET"/ham/libs/
cp "$ROOT_DIR"/ham/external/*.* "$HAM_RELEASE_TARGET"/ham/external/
cp "$ROOT_DIR"/ham/external.json "$HAM_RELEASE_TARGET"/ham/
cp "$SCRIPT_DIR"/templates/releasebuild/ham/*.sh "$HAM_RELEASE_TARGET"/ham/
cp "$SCRIPT_DIR"/templates/releasebuild/ham/*.bat "$HAM_RELEASE_TARGET"/ham/
chmod +x "$HAM_RELEASE_TARGET"/ham/*.sh