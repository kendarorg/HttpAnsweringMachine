#!/bin/sh

# Initialize
START_LOCATION=$(pwd)
. $(dirname "$0")/init.sh

# Includes
. $SCRIPT_DIR/libs/version.sh
. $SCRIPT_DIR/libs/utils.sh
. $SCRIPT_DIR/libs/docker.sh

echo This will build the docker images for the samples
echo and publish them on local docker. Ctrl+C to exit
echo Target version: $HAM_VERSION

pause

LOGIN=kendarorg
ORG=kendarorg
echo Enter $LOGIN password for $ORG
PASSWORD=$(read_password)
docker_login "$LOGIN" "$PASSWORD" "$ORG"
PASSWORD=none

# Extra initializations
ROOT_DIR=$( cd -- "$( dirname -- "$SCRIPT_DIR" )" &> /dev/null && pwd )

echo Build calendar sample images
cd $ROOT_DIR/samples/calendar/docker/multi
docker build  --rm -t ham.sampleapp.multi -f multimaster.Dockerfile ../../
docker_push "ham.sampleapp.multi" "$HAM_VERSION"
docker build  --rm -t ham.sampleapp.fe -f fe.Dockerfile ../../
docker_push "ham.sampleapp.fe" "$HAM_VERSION"
docker build  --rm -t ham.sampleapp.be -f be.Dockerfile ../../
docker_push "ham.sampleapp.be" "$HAM_VERSION"
docker build  --rm -t ham.sampleapp.gateway -f gateway.Dockerfile ../../
docker_push "ham.sampleapp.gateway" "$HAM_VERSION"

cd $ROOT_DIR/samples/calendar/docker/single
docker build -t ham.sampleapp.single -f Dockerfile ../../
docker_push "ham.sampleapp.single" "$HAM_VERSION"

echo Build quotes sample images
cd $ROOT_DIR/samples/quotes/docker/multi
docker build  --rm -t ham.quotes.master -f multimaster.Dockerfile ../../
docker_push "ham.quotes.master" "$HAM_VERSION"
docker build  --rm -t ham.quotes.core -f core.Dockerfile ../../
docker_push "ham.quotes.core" "$HAM_VERSION"