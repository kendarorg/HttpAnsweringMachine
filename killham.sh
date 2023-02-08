#!/bin/bash
HAM_MAIN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
export LANG=en_US.UTF-8
export LC_ALL=$LANG

. $HAM_MAIN_DIR/scripts/libs/version.sh
. $HAM_MAIN_DIR/scripts/libs/runner.sh

terminate_app java HttpAnsweringMachine