@echo off
set VERSION=3.0.4
set SAMPLE_VERSION=1.0.4

SET mypath=%~dp0
cd %mypath%

cd  %mypath%\ham

docker login
docker tag ham.base kendarorg/ham.base:v%VERSION%-SNAPSHOT
docker tag kendarorg/ham.base:v%VERSION%-SNAPSHOT kendarorg/ham.base:snapshot
docker push kendarorg/ham.base:v%VERSION%-SNAPSHOT
docker push kendarorg/ham.base:snapshot

docker tag ham.client kendarorg/ham.client:v%VERSION%-SNAPSHOT
docker tag kendarorg/ham.client:v%VERSION%-SNAPSHOT kendarorg/ham.client:snapshot
docker push kendarorg/ham.client:v%VERSION%-SNAPSHOT
docker push kendarorg/ham.client:snapshot

docker tag ham.apache kendarorg/ham.apache:v%VERSION%-SNAPSHOT
docker tag kendarorg/ham.apache:v%VERSION%-SNAPSHOT kendarorg/ham.apache:snapshot
docker push kendarorg/ham.apache:v%VERSION%-SNAPSHOT
docker push kendarorg/ham.apache:snapshot

docker tag ham.apache.php8 kendarorg/ham.apache.php8:v%VERSION%-SNAPSHOT
docker tag kendarorg/ham.apache.php8:v%VERSION%-SNAPSHOT kendarorg/ham.apache.php8:snapshot
docker push kendarorg/ham.apache.php8:v%VERSION%-SNAPSHOT
docker push kendarorg/ham.apache.php8:snapshot

docker tag ham.master kendarorg/ham.master:v%VERSION%-SNAPSHOT
docker tag kendarorg/ham.master:v%VERSION%-SNAPSHOT kendarorg/ham.master:snapshot
docker push kendarorg/ham.master:v%VERSION%-SNAPSHOT
docker push kendarorg/ham.master:snapshot

docker tag ham.openvpn kendarorg/ham.openvpn:v%VERSION%-SNAPSHOT
docker tag kendarorg/ham.openvpn:v%VERSION%-SNAPSHOT kendarorg/ham.openvpn:snapshot
docker push kendarorg/ham.openvpn:v%VERSION%-SNAPSHOT
docker push kendarorg/ham.openvpn:snapshot

docker tag ham.sampleapp.be kendarorg/ham.sampleapp.be:v%SAMPLE_VERSION%-SNAPSHOT
docker tag kendarorg/ham.sampleapp.be:v%SAMPLE_VERSION%-SNAPSHOT kendarorg/ham.sampleapp.be:snapshot
docker push kendarorg/ham.sampleapp.be:v%SAMPLE_VERSION%-SNAPSHOT
docker push kendarorg/ham.sampleapp.be:snapshot

docker tag ham.sampleapp.fe kendarorg/ham.sampleapp.fe:v%SAMPLE_VERSION%-SNAPSHOT
docker tag kendarorg/ham.sampleapp.fe:v%SAMPLE_VERSION%-SNAPSHOT kendarorg/ham.sampleapp.fe:snapshot
docker push kendarorg/ham.sampleapp.fe:v%SAMPLE_VERSION%-SNAPSHOT
docker push kendarorg/ham.sampleapp.fe:snapshot

docker tag ham.sampleapp.gateway kendarorg/ham.sampleapp.gateway:v%SAMPLE_VERSION%-SNAPSHOT
docker tag kendarorg/ham.sampleapp.gateway:v%SAMPLE_VERSION%-SNAPSHOT kendarorg/ham.sampleapp.gateway:snapshot
docker push kendarorg/ham.sampleapp.gateway:v%SAMPLE_VERSION%-SNAPSHOT
docker push kendarorg/ham.sampleapp.gateway:snapshot

docker tag ham.sampleapp.multi kendarorg/ham.sampleapp.multi:v%VERSION%-SNAPSHOT
docker tag kendarorg/ham.sampleapp.multi:v%VERSION%-SNAPSHOT kendarorg/ham.sampleapp.multi:snapshot
docker push kendarorg/ham.sampleapp.multi:v%VERSION%-SNAPSHOT
docker push kendarorg/ham.sampleapp.multi:snapshot

docker tag ham.quotes.master kendarorg/ham.quotes.master:v%VERSION%-SNAPSHOT
docker tag kendarorg/ham.quotes.master:v%VERSION%-SNAPSHOT kendarorg/ham.quotes.master:snapshot
docker push kendarorg/ham.quotes.master:v%VERSION%-SNAPSHOT
docker push kendarorg/ham.quotes.master:snapshot

docker tag ham.quotes.core kendarorg/ham.quotes.core:v%SAMPLE_VERSION%-SNAPSHOT
docker tag kendarorg/ham.quotes.core:v%SAMPLE_VERSION%-SNAPSHOT kendarorg/ham.quotes.core:snapshot
docker push kendarorg/ham.quotes.core:v%SAMPLE_VERSION%-SNAPSHOT
docker push kendarorg/ham.quotes.core:snapshot





