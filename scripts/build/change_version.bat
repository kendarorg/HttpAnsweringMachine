REM @echo off

set FROM_VERSION=4.2.2
set TO_VERSION=4.3.0



set START_LOCATION=%~dp0
cd %START_LOCATION%
cd ..
cd ..
set ROOT_DIR=%cd%
echo|set /p="%TO_VERSION%" > "%ROOT_DIR%\scripts\version.txt"


cd %ROOT_DIR%\scripts\libs\win

find "%ROOT_DIR%" -iname "pom.xml" -exec sed -i -b -e "s,<version>%FROM_VERSION%</version>,<version>%TO_VERSION%</version>,g" "{}" ;
find "%ROOT_DIR%" -iname "pom.xml" -exec sed -i -b -e "s,<ham.version>%FROM_VERSION%</ham.version>,<ham.version>%TO_VERSION%</ham.version>,g" "{}" ;

find "%ROOT_DIR%" -iname "pom.xml" -exec sed -i -b -e "s/\r//g" "{}" ;

find "%ROOT_DIR%" -iname "*.bat" -exec sed -i -b -e "s,HAM_VERSION=%FROM_VERSION%,HAM_VERSION=%TO_VERSION%,g" "{}" ;
find "%ROOT_DIR%" -iname "*.bat" -exec sed -i -b -e "s/\r//g" "{}" ;


find "%ROOT_DIR%" -iname "*.sh" -exec sed -i -b -e "s,HAM_VERSION=%FROM_VERSION%,HAM_VERSION=%TO_VERSION%,g" "{}" ;
find "%ROOT_DIR%" -iname "*.sh" -exec sed -i -b -e "s/\r//g" "{}" ;

find "%ROOT_DIR%" -iname "Dockerfile" -exec sed -i -b -e "s,version-%FROM_VERSION%,version-%TO_VERSION%,g" "{}" ;
find "%ROOT_DIR%" -iname "Dockerfile" -exec sed -i -b -e "s/\r//g" "{}" ;


find "%ROOT_DIR%" -iname "*.Dockerfile" -exec sed -i -b -e "s,version-%FROM_VERSION%,version-%TO_VERSION%,g" "{}" ;
find "%ROOT_DIR%" -iname "*.Dockerfile" -exec sed -i -b -e "s/\r//g" "{}" ;

del /A /F /Q /S "%ROOT_DIR%\sed*." >nul 2>&1

cd %ROOT_DIR%\scripts\build
pause