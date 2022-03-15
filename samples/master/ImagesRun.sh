#!/bin/sh

docker build -t ham.master.master .
docker-compose up
pause