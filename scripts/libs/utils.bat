@echo off
call:%~1 %~2 %~3 %~4 %~5 %~6 %~7 %~8 %~9 %~10
goto exit

:read_password
    set /p UTILS_PASSWORD=
    set "%~1=%UTILS_PASSWORD%"
goto :eof


:set_parent_dir
    set UTILS_SCRIPT_DIR=%~1
    for %%a in (%UTILS_SCRIPT_DIR:~0,1%) do set "UTILS_RETURN_DIR=%%~dpa"
    IF %UTILS_RETURN_DIR:~-1%==\ SET UTILS_RETURN_DIR=%UTILS_RETURN_DIR:~0,-1%
    set "%~2=%UTILS_RETURN_DIR%"
goto :eof

:rm_rf
    set UTILS_TODEL_DIR=%~1
    del /s /f /q "%UTILS_TODEL_DIR%\*.*"
    for /f %%f in ('dir /ad /b %UTILS_TODEL_DIR%\') do rd /s /q %UTILS_TODEL_DIR%\%%f
goto :eof


:mkdir_p
    setlocal enableextensions
    md %~1
    endlocal
goto :eof

:get_jar_name
    set UTILS_SCRIPT_DIR=%cd%
    dir /b %UTILS_SCRIPT_DIR%\*.jar > .temp.txt
    set /p UTILS_JAR_NAME=<.temp.txt
    set "%~1=%UTILS_JAR_NAME%"
    del /s /f /q .temp.txt 2>&1 1>NUL
goto :eof

:cp_r
    xcopy /e /k /h /i %~1 %~2 2>&1 1>NUL
goto :eof

:exit
exit /b