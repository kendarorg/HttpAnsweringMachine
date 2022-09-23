#!/bin/sh

VERSION_SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
export HAM_VERSION=$(head -1 "$VERSION_SCRIPT_DIR"/../version.txt)
