@ECHO OFF
SET mypath=%~dp0
cd %mypath%

:requery
set /p rundocker="Run sample docker (y/n): "
if "%rundocker%"=="n" goto simple
if "%rundocker%"=="N" goto simple
if "%rundocker%"=="y" goto docker
if "%rundocker%"=="Y" goto docker
goto requery

:docker

echo Please install OpenVpn connect (https://openvpn.net/client-connect-vpn-for-windows/)
echo Or use socks5://localhost:1080 proxy
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



cd %mypath%samples\sampleapp\gateway\target
copy /Y %mypath%samples\sampleapp\docker\application.properties.gateway %mypath%samples\sampleapp\gateway\target\application.properties
start java -jar %mypath%samples\sampleapp\gateway\target\gateway-1.0-SNAPSHOT.jar

timeout /t 10 /nobreak


cd %mypath%samples\sampleapp\be\target
copy /Y %mypath%samples\sampleapp\docker\application.properties.be %mypath%samples\sampleapp\be\target\application.properties
start java -jar %mypath%samples\sampleapp\be\target\be-1.0-SNAPSHOT.jar

timeout /t 10 /nobreak

cd %mypath%samples\sampleapp\fe\target
copy /Y %mypath%samples\sampleapp\docker\application.properties.fe %mypath%samples\sampleapp\fe\target\application.properties
start java -jar %mypath%samples\sampleapp\fe\target\fe-1.0-SNAPSHOT.jar

timeout /t 10 /nobreak

cd %mypath%ham\app\target

md "%mypath%ham\app\target\libs" 2>NUL
del /q %mypath%ham\app\target\libs\*.*
copy /Y %mypath%ham\libs\*.jar %mypath%ham\app\target\libs\

dir /b %mypath%ham\app\target\*.jar > .temp.txt
set /p APPJAR=<.temp.txt

copy /Y %mypath%samples\sampleapp\standalone.external.json %mypath%ham\app\target\external.json
start java "-Dloader.path=%mypath%ham\app\target\libs"  -Dloader.main=org.kendar.Main  ^
  	-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5025 ^
	-jar %APPJAR% org.springframework.boot.loader.PropertiesLauncher

cd %mypath%
:end

pause