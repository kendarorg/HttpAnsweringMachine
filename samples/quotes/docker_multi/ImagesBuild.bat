@echo off
docker build -t ham.sampleapp.multi -f Dockerfile.master ..\
docker build -t ham.sampleapp.fe -f Dockerfile.fe ..\
docker build -t ham.sampleapp.be -f Dockerfile.be ..\
docker build -t ham.sampleapp.gateway -f Dockerfile.gateway ..\
