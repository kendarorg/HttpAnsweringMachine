@echo off
SET mypath=%~dp0
cd %mypath%
echo %mypath%
cd base
docker build -t ham.base .


cd ../client
md "data\app" 2>NUL
del /q data\app\*.*
copy /Y ..\..\..\ham\simpledns\target\*.jar data\
docker build -t ham.client .

REM cd ../proxy
REM docker build -t ham.proxy .

cd ../openvpn
docker build -t ham.openvpn .

REM exit

cd ../master
md "data\app" 2>NUL
del /q data\app\*.*
md "data\app\libs" 2>NUL
del /q data\app\libs\*.*
copy /Y ..\..\..\ham\app\target\*.jar data\app\
copy /Y ..\..\..\ham\libs\*.jar data\app\libs\
docker build -t ham.master .

cd ../singlemaster
docker build -t ham.singlemaster .


cd ../apache
docker build -t ham.apache .

cd ../apache-php8
docker build -t ham.apache.php8 .

:end
cd ..
