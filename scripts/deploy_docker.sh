#!/bin/sh

# Initialize
START_LOCATION=$(pwd)
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

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
docker_push "ham.base" "$HAM_VERSION"
docker_push "ham.master" "$HAM_VERSION"
docker_push "ham.client" "$HAM_VERSION"
docker_push "ham.openvpn" "$HAM_VERSION"
docker_push "ham.mysql" "$HAM_VERSION"
docker_push "ham.apache" "$HAM_VERSION"
docker_push "ham.apache.php8" "$HAM_VERSION"

docker_logout
# Restore previous dir
cd $START_LOCATION