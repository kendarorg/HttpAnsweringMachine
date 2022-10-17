@echo off

REM Initialize
set START_LOCATION=%cd%
call %~dp0\init.bat

REM Includes
call %SCRIPT_DIR%\libs\version.bat
set UTILS_LIB=%SCRIPT_DIR%\libs\utils.bat

REM Extra initialisations
call %UTILS_LIB% set_parent_dir %SCRIPT_DIR% ROOT_DIR

cd %ROOT_DIR%\docs\gen_scripts
for /R %%x in (*.bat) do (
 call "%%x"
)
cd %START_LOCATION%