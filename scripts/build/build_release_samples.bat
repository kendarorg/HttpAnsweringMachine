@echo off

REM Initialize
set START_LOCATION=%cd%
call %~dp0\init.bat

REM Includes
call %SCRIPT_DIR%\libs\version.bat
set UTILS_LIB=%SCRIPT_DIR%\libs\utils.bat

echo [INFO] This will build a tar.gz with the sample applications. Ctrl+C to exit
echo [INFO] Target version: %HAM_VERSION%


REM Extra initializations
call %UTILS_LIB% set_parent_dir %SCRIPT_DIR% ROOT_DIR

REM Setup the target directory
echo [INFO] Setup target dir
set HAM_RELEASE_TARGET=%ROOT_DIR%\release\%HAM_VERSION%
call %UTILS_LIB% rm_rf %HAM_RELEASE_TARGET% > NUL  2>&1 1>NUL
set QUOTES_DIR=%ROOT_DIR%\samples\quotes
call %UTILS_LIB% mkdir_p %HAM_RELEASE_TARGET%\quotes
call %UTILS_LIB% cp_r %QUOTES_DIR%\core %HAM_RELEASE_TARGET%\quotes\


set CALENDAR_DIR=%ROOT_DIR%\samples\calendar
call %UTILS_LIB% mkdir_p %HAM_RELEASE_TARGET%\calendar
cd %CALENDAR_DIR%
call mvn clean install > %ROOT_DIR%\release\ham-%HAM_VERSION%-samples.log  2>&1

echo [INFO] Setup runner
call %UTILS_LIB% mkdir_p %HAM_RELEASE_TARGET%\calendar\scripts
call %UTILS_LIB% mkdir_p %HAM_RELEASE_TARGET%\calendar\be
copy /y %SCRIPT_DIR%\templates\releasebuild\samples\calendar\scripts\*.* %HAM_RELEASE_TARGET%\calendar\scripts 1>NUL
copy /y %SCRIPT_DIR%\templates\releasebuild\samples\calendar\*.* %HAM_RELEASE_TARGET%\calendar 1>NUL
copy /y %SCRIPT_DIR%\templates\standalone\calendar.external.json %HAM_RELEASE_TARGET%\calendar 1>NUL

echo [INFO] Setup gateway
call %UTILS_LIB% mkdir_p %HAM_RELEASE_TARGET%\calendar\gateway
cd %CALENDAR_DIR%\gateway\target\
call %UTILS_LIB% get_jar_name JAR_NAME
copy /y %SCRIPT_DIR%\templates\standalone\gateway.application.properties %HAM_RELEASE_TARGET%\calendar\gateway\application.properties 1>NUL
copy /y %CALENDAR_DIR%\gateway\target\gateway-*.jar %HAM_RELEASE_TARGET%\calendar\gateway\ 1>NUL
echo #!/bin/bash > %HAM_RELEASE_TARGET%\calendar\gateway\run.sh
echo java -jar %JAR_NAME% >> %HAM_RELEASE_TARGET%\calendar\gateway\run.sh
echo call java -jar %JAR_NAME% >> %HAM_RELEASE_TARGET%\calendar\gateway\run.bat

echo [INFO] Setup fe
call %UTILS_LIB% mkdir_p %HAM_RELEASE_TARGET%\calendar\fe
cd %CALENDAR_DIR%\fe\target\
call %UTILS_LIB% get_jar_name JAR_NAME
copy /y %SCRIPT_DIR%\templates\standalone\fe.application.properties %HAM_RELEASE_TARGET%\calendar\fe\application.properties 1>NUL
copy /y %CALENDAR_DIR%\fe\target\fe-*.jar %HAM_RELEASE_TARGET%\calendar\fe\ 1>NUL
echo #!/bin/bash > %HAM_RELEASE_TARGET%\calendar\fe\run.sh
echo java -jar %JAR_NAME% >> %HAM_RELEASE_TARGET%\calendar\fe\run.sh
echo call java -jar %JAR_NAME% >> %HAM_RELEASE_TARGET%\calendar\fe\run.bat

echo [INFO] Setup be
call %UTILS_LIB% mkdir_p %HAM_RELEASE_TARGET%\calendar\be
call %UTILS_LIB% mkdir_p %HAM_RELEASE_TARGET%\calendar\be\libs
cd %CALENDAR_DIR%\be\target\
call %UTILS_LIB% get_jar_name JAR_NAME
copy /y %SCRIPT_DIR%\templates\standalone\be.application.properties %HAM_RELEASE_TARGET%\calendar\be\application.properties 1>NUL
copy /y %SCRIPT_DIR%\templates\standalone\bedb.application.properties %HAM_RELEASE_TARGET%\calendar\be\bedb.application.properties 1>NUL
copy /y %SCRIPT_DIR%\templates\standalone\bedbham.application.properties %HAM_RELEASE_TARGET%\calendar\be\bedbham.application.properties 1>NUL
copy /y %SCRIPT_DIR%\templates\standalone\bedbhamnogen.application.properties %HAM_RELEASE_TARGET%\calendar\be\bedbhamnogen.application.properties 1>NUL
copy /y %CALENDAR_DIR%\be\target\be-*.jar %HAM_RELEASE_TARGET%\calendar\be\  1>NUL

copy /y %ROOT_DIR%\ham\libs\janus-driver*.jar %HAM_RELEASE_TARGET%\calendar\be\  1>NUL

echo #!/bin/bash > %HAM_RELEASE_TARGET%\calendar\be\run.sh
echo java -jar %JAR_NAME% >> %HAM_RELEASE_TARGET%\calendar\be\run.sh
echo call java -jar %JAR_NAME% >> %HAM_RELEASE_TARGET%\calendar\be\run.bat

REM Prepare the compressed file
echo [INFO] Compress release file
cd %ROOT_DIR%\release\%HAM_VERSION%
tar -zcvf %ROOT_DIR%\release\ham-samples-%HAM_VERSION%.tar.gz . >> %ROOT_DIR%\release\ham-%HAM_VERSION%.log  2>&1

REM Restore previous dir
cd %START_LOCATION%

