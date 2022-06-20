#!/bin/sh
function pause(){
 read -s -n 1 -p "Press any key to continue . . ."
 echo ""
}

docker build --rm -t ham.sampleapp.multi -f master.Dockerfile ../
docker build --rm -t ham.sampleapp.fe -f fe.Dockerfile ../
docker build --rm -t ham.sampleapp.be -f be.Dockerfile ../
docker build --rm -t ham.sampleapp.gateway -f gateway.Dockerfile ../
