@ECHO OFF
SET mypath=%~dp0
cd %mypath%

set /p rundocker="Run sample docker (y/n): "
if "%rundocker%"=="n" goto simple

echo Please install OpenVpn connect (https://openvpn.net/client-connect-vpn-for-windows/)
call explorer https://openvpn.net/client-connect-vpn-for-windows/
echo and import %mypath%docker\images\openvpn\mainuser.local.ovpn profile
echo then after connecting you will have full access!
pause
cd %mypath%samples\sampleapp\docker_multi
call ImagesRun.bat


goto end

:simple

echo Open with notepad (administrative rights) 
echo the file C:\Windows\System32\drivers\etc\hosts
echo and add the following lines:
echo 127.0.0.1  www.local.test
echo 127.0.0.1  www.sample.test
echo 127.0.0.1  gateway.sample.test
echo 127.0.0.1  be.sample.test

pause

cd %mypath%ham\app\target

copy /Y %mypath%samples\sampleapp\docker\external.json %mypath%ham\app\target\
start java "-Dloader.path=%mypath%ham\app\libs"  -Dloader.main=org.kendar.Main  ^
  -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5025 ^
	-jar app-1.0-SNAPSHOT.jar org.springframework.boot.loader.PropertiesLauncher
	
cd %mypath%samples\sampleapp


copy /Y %mypath%samples\sampleapp\docker\application.properties.gateway %mypath%samples\sampleapp\gateway\target\application.properties
start java -jar %mypath%samples\sampleapp\gateway\target\gateway-1.0-SNAPSHOT.jar
	
copy /Y %mypath%samples\sampleapp\docker\application.properties.be %mypath%samples\sampleapp\be\target\application.properties
start java -jar %mypath%samples\sampleapp\be\target\be-1.0-SNAPSHOT.jar
	
copy /Y %mypath%samples\sampleapp\docker\application.properties.fe %mypath%samples\sampleapp\fe\target\application.properties
start java -jar %mypath%samples\sampleapp\fe\target\fe-1.0-SNAPSHOT.jar
	
	
	
	
	
:end

pause