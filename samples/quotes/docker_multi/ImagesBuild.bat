@echo off
docker build -t ham.quotes.master -f Dockerfile.master ..\
docker build -t ham.quotes.core -f Dockerfile.core ..\
