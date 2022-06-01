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
docker build --rm -t ham.base .

# cd ../proxy
# echo Build Proxy
# docker build --rm -t ham.proxy .

#cd ../externalvpn/forticlient
#cp -f ../../base/data/startservice.sh data/
#cp -f ../../base/data/sshd_config data/
#cp -f ../../base/data/ca.crt data/
#echo Build Vpn/forticlient
#docker build --rm -t ham.forticlient .
#cd ../

#cd ../externalvpn/openconnect
#cp -f ../../base/data/startservice.sh data/
#cp -f ../../base/data/sshd_config data/
#cp -f ../../base/data/ca.crt data/
#echo Build Vpn/Openconnect
#docker build --rm -t ham.openconnect .
#cd ../

cd ../openvpn
docker build --rm -t ham.openvpn .

#exit 0

cd ../master
mkdir -p data/app
rm -rf data/app/*.*
mkdir -p data/app/libs
rm -rf data/app/libs/*.*
cp -f ../../../ham/app/target/*.jar data/app/
cp -f ../../../ham/libs/*.jar data/app/libs/
docker build --rm -t ham.master .

cd ../client
mkdir -p data/app
rm -rf data/app/*.*
cp -f  ../../../ham/simpledns/target/*.jar data/
docker build --rm -t ham.client .

cd ../singlemaster
docker build --rm -t ham.singlemaster .

cd ../apache
docker build --rm -t ham.apache .

cd ../apache-php8
docker build --rm -t ham.apache.php8 .

cd ../mysql
docker build --rm -t ham.mysql .

cd ..
