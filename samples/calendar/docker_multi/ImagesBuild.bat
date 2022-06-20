@echo off
docker build -t ham.sampleapp.multi -f master.Dockerfile ..\
docker build -t ham.sampleapp.fe -f fe.Dockerfile ..\
docker build -t ham.sampleapp.be -f be.Dockerfile ..\
docker build -t ham.sampleapp.gateway -f gateway.Dockerfile ..\
