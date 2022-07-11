@echo off
set VERSION=3.0.7
set SAMPLE_VERSION=3.0.7

exit

SET mypath=%~dp0
cd %mypath%

cd  %mypath%\ham



docker login

call :pushdata "ham.base" "%VERSION%"
call :pushdata "ham.client" "%VERSION%"
call :pushdata "ham.apache" "%VERSION%"
call :pushdata "ham.apache.php8" "%VERSION%"
call :pushdata "ham.master" "%VERSION%"
call :pushdata "ham.openvpn" "%VERSION%"
call :pushdata "ham.mysql" "%VERSION%"

call :pushdata "ham.sampleapp.be" "%SAMPLE_VERSION%"
call :pushdata "ham.sampleapp.fe" "%SAMPLE_VERSION%"
call :pushdata "ham.sampleapp.gateway" "%SAMPLE_VERSION%"
call :pushdata "ham.sampleapp.multi" "%VERSION%"


call :pushdata "ham.quotes.master" "%VERSION%"
call :pushdata "ham.quotes.core" "%SAMPLE_VERSION%"

goto :eof

:pushdata 
set IMAGE_NAME=%~1
set VERSION_NUMBER=%~2

docker tag %IMAGE_NAME% kendarorg/%IMAGE_NAME%:v%VERSION_NUMBER%
docker tag kendarorg/%IMAGE_NAME%:v%VERSION_NUMBER% kendarorg/%IMAGE_NAME%:latest
docker tag kendarorg/%IMAGE_NAME%:v%VERSION_NUMBER% kendarorg/%IMAGE_NAME%:snapshot
docker push kendarorg/%IMAGE_NAME%:v%VERSION_NUMBER%
docker push kendarorg/%IMAGE_NAME%:latest
docker push kendarorg/%IMAGE_NAME%:snapshot
goto :eof




