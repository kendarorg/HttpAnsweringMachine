@echo off

REM Initialize
set START_LOCATION=%cd%
call %~dp0\init.bat
set UTILS_LIB=%SCRIPT_DIR%\libs\utils.bat
call %UTILS_LIB% set_parent_dir %SCRIPT_DIR% ROOT_DIR

cd %ROOT_DIR%\samples\calendar\docker\multi
docker-compose up