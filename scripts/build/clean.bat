@echo off

REM Initialize
set START_LOCATION=%cd%
call %~dp0\init.bat

REM Includes
call %SCRIPT_DIR%\libs\version.bat
set UTILS_LIB=%SCRIPT_DIR%\libs\utils.bat

echo This will build a tar.gz to run the application. Ctrl+C to exit
echo Target version: %HAM_VERSION%

pause

REM Extra initializations
call %UTILS_LIB% set_parent_dir %SCRIPT_DIR% ROOT_DIR

REM Setup the target directory
echo Setup target dir
set HAM_RELEASE_TARGET=%ROOT_DIR%\release\%HAM_VERSION%
call %UTILS_LIB% rm_rf %HAM_RELEASE_TARGET%   2>&1 1>NUL
call %UTILS_LIB% mkdir_p %HAM_RELEASE_TARGET%


REM Build HAM
cd %ROOT_DIR%\ham
echo Cleaning ham
call mvn clean
call %UTILS_LIB% rm_rf %ROOT_DIR%\ham\jsplugins  2>&1 1>NUL
call %UTILS_LIB% rm_rf %ROOT_DIR%\ham\libs  2>&1 1>NUL

cd %ROOT_DIR%\samples\calendar
echo Cleaning samples
call mvn clean

call %UTILS_LIB% rm_rf %ROOT_DIR%\release  2>&1 1>NUL

REM Restore previous dir
cd %START_LOCATION%

