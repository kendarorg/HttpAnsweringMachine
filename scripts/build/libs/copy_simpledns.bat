@echo off

echo [INFO] Generate simpledns relase dirs
call %UTILS_LIB% mkdir_p %HAM_RELEASE_TARGET%\simpledns

echo [INFO] Copying simpledns to target
copy /y %ROOT_DIR%\ham\simpledns\target\simpledns-%HAM_VERSION%.jar %HAM_RELEASE_TARGET%\simpledns\  1>NUL
copy /y %SCRIPT_DIR%\templates\releasebuild\simpledns\*.sh %HAM_RELEASE_TARGET%\simpledns\  1>NUL
copy /y %SCRIPT_DIR%\templates\releasebuild\simpledns\*.bat %HAM_RELEASE_TARGET%\simpledns\  1>NUL