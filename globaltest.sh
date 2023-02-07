#!/bin/bash
HAM_MAIN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

cd $HAM_MAIN_DIR
cd scripts/build


./build_release.sh
echo build_release $?
GREP_RESULT=$(grep --include=\*.log -rnw "$HAM_MAIN_DIR/release" -e 'ERROR')
if [ -n "$GREP_RESULT" ]
then
  echo "Found...errors on ./build_release.sh"
  exit 1
fi

./build_release_samples.sh
echo build_release_samples $?
GREP_RESULT=$(grep --include=\*.log -rnw "$HAM_MAIN_DIR/release" -e 'ERROR')
if [ -n "$GREP_RESULT" ]
then
  echo "Found...errors on ./build_release_samples.sh"
  exit 1
fi

find "$HAM_MAIN_DIR/release" -name "*.tgz" -type f -exec tar xzf {} \;