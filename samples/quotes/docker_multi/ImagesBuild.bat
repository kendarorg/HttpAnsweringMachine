@echo off
docker build -t ham.quotes.master -f master.Dockerfile ..\
docker build -t ham.quotes.core -f core.Dockerfile ..\
