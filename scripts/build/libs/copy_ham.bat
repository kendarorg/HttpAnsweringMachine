@echo off

echo [INFO] Generate ham relase dirs
call %UTILS_LIB% mkdir_p %HAM_RELEASE_TARGET%\ham
call %UTILS_LIB% mkdir_p %HAM_RELEASE_TARGET%\ham\libs
call %UTILS_LIB% mkdir_p %HAM_RELEASE_TARGET%\ham\external

echo [INFO] Copying ham to target
copy /y %ROOT_DIR%\ham\app\target\app-%HAM_VERSION%.jar %HAM_RELEASE_TARGET%\ham\  1>NUL
copy /y %ROOT_DIR%\ham\libs\*.jar %HAM_RELEASE_TARGET%\ham\libs\ 1>NUL
copy /y %ROOT_DIR%\ham\external\*.* %HAM_RELEASE_TARGET%\ham\external\  1>NUL
copy /y %ROOT_DIR%\ham\*.external.json %HAM_RELEASE_TARGET%\ham\ 1>NUL
copy /y %SCRIPT_DIR%\templates\releasebuild\ham\*.bat %HAM_RELEASE_TARGET%\ham\  1>NUL
copy /y %SCRIPT_DIR%\templates\releasebuild\ham\*.sh %HAM_RELEASE_TARGET%\ham\  1>NUL