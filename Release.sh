#!/bin/sh

VERSION=3.0.7

mypath=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )/
cd $mypath

./Make.sh

rm -rf "$mypath"docker/images/client/data/app
rm  -f "$mypath"docker/images/client/data/*.jar
rm -rf "$mypath"docker/images/master/data/app

rm -rf "$mypath"release/target
mkdir -p "$mypath"release/target
mkdir -p "$mypath"release/target/libs
mkdir -p "$mypath"release/target/external
mkdir -p "$mypath"release/target/docker


echo Preparing Jars
cp "$mypath"ham/app/target/app-"$VERSION".jar "$mypath"release/target/
cp "$mypath"ham/simpledns/target/simpledns-"$VERSION".jar "$mypath"release/target/
cp "$mypath"ham/libs/*.jar "$mypath"release/target/libs/
cp "$mypath"ham/external/*.* "$mypath"release/target/external/
cp "$mypath"ham/external.json "$mypath"release/target/
echo Preparing Docker
cp -R "$mypath"docker/images "$mypath"release/target/docker
rm -f "$mypath"release/target/docker/*.bat
rm -f "$mypath"release/target/docker/*.sh

rm -rf "$mypath"release/target/docker/externalvpn
cp "$mypath"release/ImagesBuild.* "$mypath"release/target/docker
cp "$mypath"release/Run.* "$mypath"release/target/
