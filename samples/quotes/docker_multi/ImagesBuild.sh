#!/bin/sh
function pause(){
 read -s -n 1 -p "Press any key to continue . . ."
 echo ""
}

docker build --rm -t ham.quotes.master -f master.Dockerfile ../
docker build --rm -t ham.quotes.core -f core.Dockerfile ..\
