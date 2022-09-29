#!/bin/sh

# Initialize
START_LOCATION=$(pwd)
. $(dirname "$0")/init.sh

# Includes
. $SCRIPT_DIR/libs/version.sh
. $SCRIPT_DIR/libs/utils.sh

echo This will build the docker images for the samples
echo and publish them on local docker. Ctrl+C to exit
echo Target version: $HAM_VERSION

pause

# Extra initializations
ROOT_DIR=$( cd -- "$( dirname -- "$SCRIPT_DIR" )" &> /dev/null && pwd )

echo Build calendar sample images
cd $ROOT_DIR/samples/calendar/docker/multi
docker build  --rm -t ham.sampleapp.multi -f multimaster.Dockerfile ../../
docker build  --rm -t ham.sampleapp.fe -f fe.Dockerfile ../../
docker build  --rm -t ham.sampleapp.be -f be.Dockerfile ../../
docker build  --rm -t ham.sampleapp.gateway -f gateway.Dockerfile ../../

cd $ROOT_DIR/samples/calendar/docker/single
docker build -t ham.sampleapp.single -f Dockerfile ../../

echo Build quotes sample images
cd $ROOT_DIR/samples/quotes/docker/multi
docker build  --rm -t ham.quotes.master -f master.Dockerfile ../../
docker build  --rm -t ham.quotes.core -f core.Dockerfile ../../