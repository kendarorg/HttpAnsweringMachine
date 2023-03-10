#!/bin/bash

# Initialize
START_LOCATION=$(pwd)
. $(dirname "$0")/init.sh

# Includes
. $SCRIPT_DIR/libs/version.sh
. $SCRIPT_DIR/libs/utils.sh
. $SCRIPT_DIR/libs/docker.sh

echo [INFO] This will build the docker images for the application
echo [INFO] and publish them on local docker. Ctrl+C to exit
echo [INFO] Target version: $HAM_VERSION

LOGIN=kendarorg
ORG=kendarorg
PASSWORD=none

if [ "$DOCKER_DEPLOY" == "true" ]; then
  pause
echo Enter $LOGIN password for $ORG
PASSWORD=$(read_password)
docker_login "$LOGIN" "$PASSWORD" "$ORG"
PASSWORD=none
fi

# Extra initializations
ROOT_DIR=$( cd -- "$( dirname -- "$SCRIPT_DIR" )" &> /dev/null && pwd )
DOCKER_ROOT=$ROOT_DIR/docker/images
HAM_DIR=$ROOT_DIR/ham
HAM_LIBS_DIR=$HAM_DIR/libs

# Init variables
ROOT_PWD=root
HAM_DEBUG=false
DNS_HIJACK_SERVER=THEDOCKERNAMEOFTHERUNNINGMASTER

cd $HAM_DIR

cd $DOCKER_ROOT/base
docker build  -t ham.base .
docker_push "ham.base" "$HAM_VERSION"

cd $DOCKER_ROOT/client
mkdir -p data/app || true
rm -f data/app/*.*
cp -f "$HAM_DIR"/simpledns/target/simpledns*.jar data/
docker build  -t ham.client .
docker_push "ham.client" "$HAM_VERSION"
rm -rf data/app
rm -f data/simpledns*.jar

cd $DOCKER_ROOT/openvpn
docker build  -t ham.openvpn .
docker_push "ham.openvpn" "$HAM_VERSION"

cd $DOCKER_ROOT/master
mkdir -p data/app
rm -rf data/app/*.*
mkdir -p data/app/libs
rm -rf data/app/libs/*.*
cp -f "$HAM_DIR"/app/target/app*.jar data/app/
cp -f "$HAM_LIBS_DIR"/*.jar data/app/libs/
docker build  -t ham.master .
docker_push "ham.master" "$HAM_VERSION"
rm -rf data/app

cd $DOCKER_ROOT/singlemaster
docker build  -t ham.singlemaster .
docker_push "ham.singlemaster" "$HAM_VERSION"

cd $DOCKER_ROOT/apache
docker build  -t ham.apache .
docker_push "ham.apache" "$HAM_VERSION"

cd $DOCKER_ROOT/apache-php8
docker build  -t ham.apache.php8 .
docker_push "ham.apache.php8" "$HAM_VERSION"

cd $DOCKER_ROOT/mysql
docker build  -t ham.mysql .
docker_push "ham.mysql" "$HAM_VERSION"

# Restore previous dir
cd $START_LOCATION
