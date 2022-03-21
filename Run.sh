
mypath="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"\
cd $mypath

function pause(){
 read -s -n 1 -p "Press any key to continue . . ."
 echo ""
}


while true; do
    read -p "Run sample docker (y/n): " yn
    case $yn in
        [Yy]* ) { rundocker="y" ; break ; } ;;
        [Nn]* ) { rundocker="n" ; break ; } ;;
        * ) echo "Please answer yes or no.";;
    esac
done


if [ "$rundocker" == "y" ]; then
	
	
	echo Please install OpenVpn connect \(https://openvpn.net/vpn-client/\)
	pause
	echo and import $mypathdocker/images/openvpn/mainuser.local.ovpn profile
	echo then after connecting you will have full access!
	pause
	cd $mypathsamples/sampleapp/docker_multi
	chmod 777 *.sh
	./ImagesRun.sh
fi

if [ "$rundocker" == "n" ]; then

	echo Open with sudo vi /etc/hosts
	echo and add the following lines:
	echo 127.0.0.1  www.local.test
	echo 127.0.0.1  www.sample.test
	echo 127.0.0.1  gateway.sample.test
	echo 127.0.0.1  be.sample.test
 
	pause

	cd $mypathham/app/target

	cp -f $mypathsamples/sampleapp/docker/external.json $mypathham/app/target/
	java "-Dloader.path=$mypathham/app/libs"  -Dloader.main=org.kendar.Main  ^
	  -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5025 ^
		-jar app-1.0-SNAPSHOT.jar org.springframework.boot.loader.PropertiesLauncher &
		
	cd $mypathsamples/sampleapp


	cp -f $mypathsamples/sampleapp/docker/application.properties.gateway $mypathsamples/sampleapp/gateway/target/application.properties
	java -jar $mypathsamples/sampleapp/gateway/target/gateway-1.0-SNAPSHOT.jar &
		
	cp -f $mypathsamples/sampleapp/docker/application.properties.be $mypathsamples/sampleapp/be/target/application.properties
	java -jar $mypathsamples/sampleapp/be/target/be-1.0-SNAPSHOT.jar &
		
	cp -f $mypathsamples/sampleapp/docker/application.properties.fe $mypathsamples/sampleapp/fe/target/application.properties
	java -jar $mypathsamples/sampleapp/fe/target/fe-1.0-SNAPSHOT.jar &
	
fi

pause