@echo off

REM Initialize
set START_LOCATION=%cd%
call %~dp0\init.bat

REM Includes
call %SCRIPT_DIR%\libs\version.bat
set UTILS_LIB=%SCRIPT_DIR%\libs\utils.bat

echo This will publish jars on the kendar maven repo. Ctrl+C to exit
echo Target version: %HAM_VERSION%

pause