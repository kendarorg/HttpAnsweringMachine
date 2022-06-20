#!/bin/sh

mypath=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )/

cd $mypath

echo THIS CAN BE RUN ON DOCKER ONLY!

function pause(){
 read -s -n 1 -p "Press any key to continue . . ."
 echo ""
}
	
	
	echo Please install OpenVpn connect \(https://openvpn.net/vpn-client/\)
	echo Or use socks5://localhost:1080 proxy
	pause
	echo and import "$mypath"docker/images/openvpn/mainuser.local.ovpn profile
	echo then after connecting you will have full access!
	pause
	cd "$mypath"samples/quotes/docker_multi
	chmod 777 *.sh
	./ImagesRun.sh

