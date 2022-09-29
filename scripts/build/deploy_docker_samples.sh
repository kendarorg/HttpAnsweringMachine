#!/bin/sh

# Initialize
START_LOCATION=$(pwd)
. $(dirname "$0")/init.sh

# Includes
. $SCRIPT_DIR/libs/version.sh
. $SCRIPT_DIR/libs/utils.sh
. $SCRIPT_DIR/libs/docker.sh

echo This will publish the docker images for the application
echo from the local docker repo. Ctrl+C to exit
echo Target version: $HAM_VERSION

pause

# Extra initializations
ROOT_DIR=$( cd -- "$( dirname -- "$SCRIPT_DIR" )" &> /dev/null && pwd )

LOGIN=kendarorg
ORG=kendarorg
echo Enter $LOGIN password for $ORG
PASSWORD=$(read_password)
docker_login "$LOGIN" "$PASSWORD" "$ORG"
PASSWORD=none
docker_push "ham.sampleapp.be" "$HAM_VERSION"
docker_push "ham.sampleapp.fe" "$HAM_VERSION"
docker_push "ham.sampleapp.gateway" "$HAM_VERSION"
docker_push "ham.sampleapp.multi" "$HAM_VERSION"
docker_push "ham.sampleapp.single" "$HAM_VERSION"

docker_push "ham.quotes.master" "$HAM_VERSION"
docker_push "ham.quotes.core" "$HAM_VERSION"

docker_logout
# Restore previous dir
cd $START_LOCATION