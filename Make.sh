#!/bin/sh

mypath="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $mypath

echo Building HAM
cd ham
call mvn clean install
cd ..
pause
echo Building sample applications
cd samples\sampleapp
call mvn clean install
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