@ECHO OFF
SET mypath=%~dp0
cd %mypath%

copy /Y %mypath%samples\sampleapp\standalone.external.json %mypath%ham\app\target\external.json
start java "-Dloader.path=%mypath%ham\app\target\libs"  -Dloader.main=org.kendar.Main  ^
  	-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5025 ^
	-jar %APPJAR% org.springframework.boot.loader.PropertiesLauncher

cd %mypath%
:end

pause