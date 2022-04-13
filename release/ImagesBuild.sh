#!/bin/sh

mypath=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )/

cd $mypath

cd base
docker build -t ham.base .


cd ../client
md "data/app" 2>NUL
rm -f data\app\*.*
cp -f ../simpledns*.jar data/
docker build -t ham.client .

cd ../proxy
docker build -t ham.proxy .


cd ../openvpn
docker build -t ham.openvpn .

cd ../master
mkdir -p data/app
rm -rf data/app/*.*
mkdir -p data/app/libs
rm -rf data/app/libs/*.*
cp -f ../app*.jar data/app/
cp -f ../libs/*.jar data/app/libs/
docker build -t ham.master .

cd ../singlemaster
docker build -t ham.singlemaster .

cd ..