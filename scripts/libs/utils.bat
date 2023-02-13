@echo off
call:%~1 %~2 %~3 %~4 %~5 %~6 %~7 %~8 %~9 %~10
goto exit

:read_password
    set /p UTILS_PASSWORD=
    set "%~1=%UTILS_PASSWORD%"
goto :eof


:set_parent_dir
    set UTILS_CURDIR=%cd%
    set UTILS_SCRIPT_DIR=%~1
    cd %UTILS_SCRIPT_DIR%
    cd ..
    set UTILS_RETURN_DIR=%cd%
    REM for %%a in (%UTILS_SCRIPT_DIR:~0,1%) do set "UTILS_RETURN_DIR=%%~dpa"
    cd %UTILS_CURDIR%
    IF %UTILS_RETURN_DIR:~-1%==\ SET UTILS_RETURN_DIR=%UTILS_RETURN_DIR:~0,-1%
    set "%~2=%UTILS_RETURN_DIR%"
goto :eof

:get_time
    set STARTTIME=%TIME%
    set /A STARTTIME=(1%STARTTIME:~0,2%-100)*3600 + (1%STARTTIME:~3,2%-100)*60 + (1%STARTTIME:~6,2%-100)
    set "%~1=%STARTTIME%"
goto :eof

:rm_rf
    set UTILS_TODEL_DIR=%~1
    IF exist %UTILS_TODEL_DIR% (
        del /s /f /q "%UTILS_TODEL_DIR%\*.*"
        for /f %%f in ('dir /ad /b %UTILS_TODEL_DIR%\') do rd /s /q %UTILS_TODEL_DIR%\%%f
        rmdir /S /Q %UTILS_TODEL_DIR%
    )
goto :eof


:mkdir_p
    setlocal enableextensions
    if not exist %~1 md %~1
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