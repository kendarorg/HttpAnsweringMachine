#!/bin/sh

function pause(){
 read -s -n 1 -p "Press any key to continue . . ."
 echo ""
}

ROOT_PWD=root
HAM_DEBUG=false
DNS_HIJACK_SERVER=THEDOCKERNAMEOFTHERUNNINGMASTER

cd base
echo Build Base
docker build -t ham.base .

#cd ../externalvpn/forticlient
#cp -f ../../base/data/startservice.sh data/
#cp -f ../../base/data/sshd_config data/
#cp -f ../../base/data/ca.crt data/
#echo Build Vpn/forticlient
#docker build -t ham.forticlient .
#cd ../

#cd ../externalvpn/openconnect
#cp -f ../../base/data/startservice.sh data/
#cp -f ../../base/data/sshd_config data/
#cp -f ../../base/data/ca.crt data/
#echo Build Vpn/Openconnect
#docker build -t ham.openconnect .
#cd ../

cd ../openvpn
docker build -t ham.openvpn .

#exit 0

cd ../master
mkdir -p data/app
rm -rf data/app/*.*
mkdir -p data/app/libs
rm -rf data/app/libs/*.*
cp -f ../../../ham/app/target/*.jar data/app/
cp -f ../../../ham/libs/*.jar data/app/libs/
docker build -t ham.master .

cd ../client
mkdir -p data/app
rm -rf data/app/*.*
cp -f  ../../../ham/simpledns/target/*.jar data/
docker build -t ham.client .

cd ../proxy
mkdir -p cliendata
rm -rf cliendata/*.*
md "cliendata" 2>NUL
cp -f -r  ../base/data clientdata
cp -f -r  ../client/data clientdata
docker build -t ham.proxy .

cd ..
pause