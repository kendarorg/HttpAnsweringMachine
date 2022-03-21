﻿set ROOT_PWD=root
set HAM_DEBUG=false
set DNS_HIJACK_SERVER=THEDOCKERNAMEOFTHERUNNINGMASTER

cd base
docker build -t ham.base .

REM cd ..\externalvpn\forticlient
REM copy /Y ..\..\base\data\startservice.sh data/
REM copy /Y ..\..\base\data\sshd_config data/
REM copy /Y ..\..\base\data\ca.crt data/
REM echo Build Vpn/forticlient
REM docker build -t ham.forticlient .
REM cd ..\


REM cd ..\externalvpn\openconnect
REM copy /Y ..\..\base\data\startservice.sh data/
REM copy /Y ..\..\base\data\sshd_config data/
REM copy /Y ..\..\base\data\ca.crt data/
REM echo Build Vpn/Openconnect
REM docker build -t ham.openconnect .
REM cd ..\


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

cd ../client
md "data\app" 2>NUL
del /q data\app\*.*
copy /Y ..\..\..\ham\simpledns\target\*.jar data\
docker build -t ham.client .

:end
cd ..
pause