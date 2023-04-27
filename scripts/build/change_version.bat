REM @echo off

set FROM_VERSION=4.2.2
set TO_VERSION=4.3.0



set START_LOCATION=%~dp0
cd %START_LOCATION%
cd ..
cd ..
set ROOT_DIR=%cd%
cd %ROOT_DIR%\scripts\libs\win

find "%ROOT_DIR%" -iname "*.xml" -exec sed -i -b -e "s,<version>%FROM_VERSION%</version>,<version>%TO_VERSION%</version>,g" "{}" ;

