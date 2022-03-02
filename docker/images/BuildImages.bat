set ROOT_PWD=root
set HAM_DEBUG=false
set DNS_HIJACK_SERVER=THEDOCKERNAMEOFTHERUNNINGMASTER

cd base
docker build -t ham.base .

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

cd ../openvpn
docker build -t ham.openvpn .

:end
cd ..
pause