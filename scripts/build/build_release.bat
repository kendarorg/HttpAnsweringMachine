@echo off

REM Initialize
set START_LOCATION=%cd%
call %~dp0\init.bat

REM Includes
call %SCRIPT_DIR%\libs\version.bat
set UTILS_LIB=%SCRIPT_DIR%\libs\utils.bat

echo [INFO] This will build a tar.gz to run the application. Ctrl+C to exit
echo [INFO] Target version: %HAM_VERSION%


REM Extra initializations
call %UTILS_LIB% set_parent_dir %SCRIPT_DIR% ROOT_DIR

REM Setup the target directory
echo [INFO] Setup target dir
set HAM_RELEASE_TARGET=%ROOT_DIR%\release\%HAM_VERSION%
call %UTILS_LIB% rm_rf %HAM_RELEASE_TARGET%   2>&1 1>NUL
call %UTILS_LIB% mkdir_p %HAM_RELEASE_TARGET%


REM Build HAM
cd %ROOT_DIR%\ham
echo [INFO] Building ham
call mvn clean install -DskipTests > %ROOT_DIR%\release\ham-%HAM_VERSION%.log  2>&1



echo [INFO] Copying result to target
call %~dp0\libs\copy_ham.bat
call %~dp0\libs\copy_simpledns.bat

REM Prepare the compressed file
echo [INFO] Compress release file
cd %ROOT_DIR%\release\%HAM_VERSION%
tar -zcvf %ROOT_DIR%\release\ham-%HAM_VERSION%.tar.gz . >> %ROOT_DIR%\release\ham-%HAM_VERSION%.log  2>&1


REM Restore previous dir
cd %START_LOCATION%

