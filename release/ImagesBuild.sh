#!/bin/sh

mypath=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )/

cd $mypath

cd base
docker build --rm -t ham.base .


cd ../client
md "data/app" 2>NUL
rm -f data\app\*.*
cp -f ../simpledns*.jar data/
docker build --rm -t ham.client .

cd ../proxy
docker build --rm -t ham.proxy .


cd ../openvpn
docker build --rm -t ham.openvpn .

cd ../master
mkdir -p data/app
rm -rf data/app/*.*
mkdir -p data/app/libs
rm -rf data/app/libs/*.*
cp -f ../app*.jar data/app/
cp -f ../libs/*.jar data/app/libs/
docker build --rm -t ham.master .

cd ../singlemaster
docker build --rm -t ham.singlemaster .

cd ..