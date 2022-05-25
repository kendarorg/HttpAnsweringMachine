@echo off
set VERSION=3.0.4
set SAMPLE_VERSION=1.0.4

SET mypath=%~dp0
cd %mypath%

cd  %mypath%\ham

docker login
docker tag ham.base kendarorg/ham.base:v%VERSION%
docker tag kendarorg/ham.base:v%VERSION% kendarorg/ham.base:latest
docker push kendarorg/ham.base:v%VERSION%
docker push kendarorg/ham.base:latest

docker tag ham.client kendarorg/ham.client:v%VERSION%
docker tag kendarorg/ham.client:v%VERSION% kendarorg/ham.client:latest
docker push kendarorg/ham.client:v%VERSION%
docker push kendarorg/ham.client:latest

docker tag ham.apache kendarorg/ham.apache:v%VERSION%
docker tag kendarorg/ham.apache:v%VERSION% kendarorg/ham.apache:latest
docker push kendarorg/ham.apache:v%VERSION%
docker push kendarorg/ham.apache:latest

docker tag ham.apache.php8 kendarorg/ham.apache.php8:v%VERSION%
docker tag kendarorg/ham.apache.php8:v%VERSION% kendarorg/ham.apache.php8:latest
docker push kendarorg/ham.apache.php8:v%VERSION%
docker push kendarorg/ham.apache.php8:latest

docker tag ham.master kendarorg/ham.master:v%VERSION%
docker tag kendarorg/ham.master:v%VERSION% kendarorg/ham.master:latest
docker push kendarorg/ham.master:v%VERSION%
docker push kendarorg/ham.master:latest

docker tag ham.openvpn kendarorg/ham.openvpn:v%VERSION%
docker tag kendarorg/ham.openvpn:v%VERSION% kendarorg/ham.openvpn:latest
docker push kendarorg/ham.openvpn:v%VERSION%
docker push kendarorg/ham.openvpn:latest

docker tag ham.sampleapp.be kendarorg/ham.sampleapp.be:v%SAMPLE_VERSION%
docker tag kendarorg/ham.sampleapp.be:v%SAMPLE_VERSION% kendarorg/ham.sampleapp.be:latest
docker push kendarorg/ham.sampleapp.be:v%SAMPLE_VERSION%
docker push kendarorg/ham.sampleapp.be:latest

docker tag ham.sampleapp.fe kendarorg/ham.sampleapp.fe:v%SAMPLE_VERSION%
docker tag kendarorg/ham.sampleapp.fe:v%SAMPLE_VERSION% kendarorg/ham.sampleapp.fe:latest
docker push kendarorg/ham.sampleapp.fe:v%SAMPLE_VERSION%
docker push kendarorg/ham.sampleapp.fe:latest

docker tag ham.sampleapp.gateway kendarorg/ham.sampleapp.gateway:v%SAMPLE_VERSION%
docker tag kendarorg/ham.sampleapp.gateway:v%SAMPLE_VERSION% kendarorg/ham.sampleapp.gateway:latest
docker push kendarorg/ham.sampleapp.gateway:v%SAMPLE_VERSION%
docker push kendarorg/ham.sampleapp.gateway:latest

docker tag ham.sampleapp.multi kendarorg/ham.sampleapp.multi:v%VERSION%
docker tag kendarorg/ham.sampleapp.multi:v%VERSION% kendarorg/ham.sampleapp.multi:latest
docker push kendarorg/ham.sampleapp.multi:v%VERSION%
docker push kendarorg/ham.sampleapp.multi:latest

docker tag ham.quotes.master kendarorg/ham.quotes.master:v%VERSION%
docker tag kendarorg/ham.quotes.master:v%VERSION% kendarorg/ham.quotes.master:latest
docker push kendarorg/ham.quotes.master:v%VERSION%
docker push kendarorg/ham.quotes.master:latest

docker tag ham.quotes.core kendarorg/ham.quotes.core:v%SAMPLE_VERSION%
docker tag kendarorg/ham.quotes.core:v%SAMPLE_VERSION% kendarorg/ham.quotes.core:latest
docker push kendarorg/ham.quotes.core:v%SAMPLE_VERSION%
docker push kendarorg/ham.quotes.core:latest





