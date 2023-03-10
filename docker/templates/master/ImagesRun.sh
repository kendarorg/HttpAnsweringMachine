#!/bin/sh

# TODO Set the application name
export YOUR_APP_NAME=test
export YOUR_APP_FULL_PATH=/home/username/test/target

while true; do
    read -p "Build docker image (y/N): " yn
    case $yn in
        [Yy]* ) { builddocker="y" ; break ; } ;;
        [Nn]* ) { builddocker="n" ; break ; } ;;
        * ) { builddocker="n" ; break ; } ;;
    esac
done

if [ "$builddocker" == "y" ]; then
	docker build  -t "app.$YOUR_APP_NAME" .
fi


docker run --name "$YOUR_APP_NAME" --privileged \
	--cap-add SYS_ADMIN --cap-add DAC_READ_SEARCH \
	-v "$YOUR_APP_FULL_PATH" /app ^
	--dns=127.0.0.1 "app.$YOUR_APP_NAME"