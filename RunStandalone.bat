@ECHO OFF
SET mypath=%~dp0
cd %mypath%




echo run chrome with: --proxy-server="socks5://localhost:1080"
REM C:\Data\PortableApps\PortableApps\GoogleChromePortable\GoogleChromePortable.exe --proxy-server="socks5://localhost:1080"

pause


cd %mypath%ham\app\target

md "%mypath%ham\app\target\libs" 2>NUL
del /q %mypath%ham\app\target\libs\*.*
copy /Y %mypath%ham\libs\*.jar %mypath%ham\app\target\libs\

dir /b %mypath%ham\app\target\*.jar > .temp.txt
set /p APPJAR=<.temp.txt


md "%mypath%ham\app\target\external" 2>NUL
copy /Y %mypath%ham\*.json %mypath%ham\app\target\
copy /Y %mypath%ham\external\*.json %mypath%ham\app\target\external\
start java "-Dloader.path=%mypath%ham\app\target\libs"  -Dloader.main=org.kendar.Main  ^
  	-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5025 ^
	-jar %APPJAR% org.springframework.boot.loader.PropertiesLauncher

cd %mypath%
:end

pause