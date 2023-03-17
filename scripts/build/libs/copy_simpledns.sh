#!/bin/bash

echo [INFO] Generate simpledns relase dirs
mkdir -p $HAM_RELEASE_TARGET/simpledns

echo [INFO] Copying simpledns to target
cp "$ROOT_DIR"/ham/simpledns/target/simpledns-"$HAM_VERSION".jar "$HAM_RELEASE_TARGET"/simpledns/
cp "$SCRIPT_DIR"/templates/releasebuild/simpledns/*.sh "$HAM_RELEASE_TARGET"/simpledns/
cp "$SCRIPT_DIR"/templates/releasebuild/simpledns/*.bat "$HAM_RELEASE_TARGET"/simpledns/
chmod +x "$HAM_RELEASE_TARGET"/simpledns/*.sh