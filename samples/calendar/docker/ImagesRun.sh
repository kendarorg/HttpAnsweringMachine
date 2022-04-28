#!/bin/sh

function pause(){
 read -s -n 1 -p "Press any key to continue . . ."
 echo ""
}

docker build -t ham.sampleapp.single -f Dockerfile ../
docker-compose up
pause