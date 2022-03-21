#!/bin/sh

mypath="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"\
cd $mypath

function pause(){
 read -s -n 1 -p "Press any key to continue . . ."
 echo ""
}

echo Building HAM
cd ham
mvn clean install
cd ..
pause
echo Building sample applications
cd samples/sampleapp
mvn clean install
cd ..
cd ..
pause

while true; do
    read -p "Build docker images (y/n): " yn
    case $yn in
        [Yy]* ) { builddocker="y" ; break ; } ;;
        [Nn]* ) { builddocker="n" ; break ; } ;;
        * ) echo "Please answer yes or no.";;
    esac
done

if [ "$builddocker" == "y" ]; then
    echo Building main docker images
	cd docker/images
	./ImagesBuild.sh
	cd .. 
	cd ..

	echo Building sampleapp docker images
	cd samples/sampleapp/docker_multi
	./ImagesBuild.sh
	cd ..
	cd ..
fi



pause