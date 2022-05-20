@ECHO OFF
SET mypath=%~dp0
cd %mypath%

This can be run only with docker!

:docker

echo Please install OpenVpn connect (https://openvpn.net/client-connect-vpn-for-windows/)
echo Or use socks5://localhost:1080 proxy
REM call explorer https://openvpn.net/client-connect-vpn-for-windows/
echo and import %mypath%docker\images\openvpn\mainuser.local.ovpn profile
echo then after connecting you will have full access!
pause
cd %mypath%samples\quotes\docker_multi
call ImagesRun.bat