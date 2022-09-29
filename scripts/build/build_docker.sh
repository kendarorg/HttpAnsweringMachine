#!/bin/sh

# Initialize
START_LOCATION=$(pwd)
. $(dirname "$0")/init.sh

# Includes
. $SCRIPT_DIR/libs/version.sh
. $SCRIPT_DIR/libs/utils.sh

echo This will build the docker images for the application
echo and publish them on local docker. Ctrl+C to exit
echo Target version: $HAM_VERSION

pause

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

echo Building project
mvn clean install

cd $DOCKER_ROOT/base
docker build --rm -t ham.base .

cd $DOCKER_ROOT/client
mkdir -p data/app || true
rm -f data/app/*.*
cp -f "$HAM_DIR"/simpledns/target/simpledns*.jar data/
docker build --rm -t ham.client .
rm -rf data/app
rm -f data/simpledns*.jar

cd $DOCKER_ROOT/openvpn
docker build --rm -t ham.openvpn .

cd $DOCKER_ROOT/master
mkdir -p data/app
rm -rf data/app/*.*
mkdir -p data/app/libs
rm -rf data/app/libs/*.*
cp -f "$HAM_DIR"/app/target/app*.jar data/app/
cp -f "$HAM_LIBS_DIR"/*.jar data/app/libs/
docker build --rm -t ham.master .
rm -rf data/app

cd $DOCKER_ROOT/singlemaster
docker build --rm -t ham.singlemaster .

cd $DOCKER_ROOT/apache
docker build --rm -t ham.apache .

cd $DOCKER_ROOT/apache-php8
docker build --rm -t ham.apache.php8 .

cd $DOCKER_ROOT/mysql
docker build --rm -t ham.mysql .

echo Cleanup
cd $HAM_DIR
mvn clean > /dev/null 2>1

# Restore previous dir
cd $START_LOCATION
