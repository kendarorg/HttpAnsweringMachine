@echo off

REM Initialize
set START_LOCATION=%cd%
set SCRIPT_DIR=%~dp0

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
call %UTILS_LIB% mkdir_p %HAM_RELEASE_TARGET%\ham
call %UTILS_LIB% mkdir_p %HAM_RELEASE_TARGET%\simpledns
call %UTILS_LIB% mkdir_p %HAM_RELEASE_TARGET%\ham\libs
call %UTILS_LIB% mkdir_p %HAM_RELEASE_TARGET%\ham\external


REM Build HAM
cd %ROOT_DIR%\ham
echo Building ham
call mvn clean install > %ROOT_DIR%\release\ham-%HAM_VERSION%.log  2>&1



REM copy /y result
echo Copying result to target
copy /y %ROOT_DIR%\ham\app\target\app-%HAM_VERSION%.jar %HAM_RELEASE_TARGET%\ham\  1>NUL
copy /y %ROOT_DIR%\ham\simpledns\target\simpledns-%HAM_VERSION%.jar %HAM_RELEASE_TARGET%\simpledns\  1>NUL
copy /y %ROOT_DIR%\ham\libs\*.jar %HAM_RELEASE_TARGET%\ham\libs\ 1>NUL
copy /y %ROOT_DIR%\ham\external\*.* %HAM_RELEASE_TARGET%\ham\external\  1>NUL
copy /y %ROOT_DIR%\ham\external.json %HAM_RELEASE_TARGET%\ham\ 1>NUL

REM Prepare the run commands
copy /y %SCRIPT_DIR%\templates\releasebuild\ham\*.sh %HAM_RELEASE_TARGET%\ham\  1>NUL
copy /y %SCRIPT_DIR%\templates\releasebuild\simpledns\*.sh %HAM_RELEASE_TARGET%\simpledns\  1>NUL
copy /y %SCRIPT_DIR%\templates\releasebuild\ham\*.bat %HAM_RELEASE_TARGET%\ham\  1>NUL
copy /y %SCRIPT_DIR%\templates\releasebuild\simpledns\*.bat %HAM_RELEASE_TARGET%\simpledns\  1>NUL



REM Prepare the compressed file
echo Compress release file
cd %ROOT_DIR%\release\%HAM_VERSION%
tar -zcvf %ROOT_DIR%\release\ham-%HAM_VERSION%.tar.gz . >> %ROOT_DIR%\release\ham-%HAM_VERSION%.log  2>&1

REM Cleanup
echo Cleanup
call %UTILS_LIB% rm_rf %HAM_RELEASE_TARGET%  2>&1 1>NUL

REM Restore previous dir
cd %START_LOCATION%

