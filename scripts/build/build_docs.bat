@echo off

REM Initialize
set START_LOCATION=%cd%
call %~dp0\init.bat

REM Includes
call %SCRIPT_DIR%\libs\version.bat
set UTILS_LIB=%SCRIPT_DIR%\libs\utils.bat

REM Extra initialisations
call %UTILS_LIB% set_parent_dir %SCRIPT_DIR% ROOT_DIR
call %ROOT_DIR%\docs\gen_scripts\localsample.bat
call %ROOT_DIR%\docs\gen_scripts\googlehack.bat
call %ROOT_DIR%\docs\gen_scripts\googlehack_android.bat
call %ROOT_DIR%\docs\gen_scripts\manualtestcalendar.bat
call %ROOT_DIR%\docs\gen_scripts\automatictestcalendar.bat