@echo off
SET mypath=%~dp0
cd %mypath%
echo %mypath%
cd base
docker build -t ham.base .


cd ../client
md "data\app" 2>NUL
del /q data\app\*.*
copy /Y ..\simpledns*.jar data\
docker build -t ham.client .

cd ../proxy
docker build -t ham.proxy .


cd ../openvpn
docker build -t ham.openvpn .


cd ../master
md "data\app" 2>NUL
del /q data\app\*.*
md "data\app\libs" 2>NUL
del /q data\app\libs\*.*
copy /Y ..\app*.jar data\app\
copy /Y ..\libs\*.jar data\app\libs\
docker build -t ham.master .

cd ../singlemaster
docker build -t ham.singlemaster .

:end
cd ..
pause