## Call function and pass params

Caller function. Notice that the return variable name is passed
as last param
<pre>
    call libBatch.bat read_password PASSWD
    echo %PASSWD%
</pre>

Called function. The preamble and the end are mandatory for every
function
<pre>
    @echo off
    call:%~1 %~2 %~3 %~4 %~5 %~6 %~7 %~8 %~9 %~10
    goto exit
    
    :read_password
        set /p password=
        set "%~1=%password%"
    goto :eof
    
    
    :exit
    exit /b
</pre>

## Get parent path

* Setting 0,1 means RETURN_DIR==CURRENT_DIR ( C:\lev0\lev1\lev2\ )
* Setting 0,2 means RETURN_DIR==CURRENT_DIR-1 ( C:\lev0\lev1\ )

<pre>
set CURRENT_DIR=C:\lev0\lev1\lev2

for %%a in (%SCRIPT_DIR:~0,1%) do set "RETURN_DIR=%%~dpa"


</pre>

## REmove trailing slash

<pre>
::Does string have a trailing slash? if so remove it 
IF %datapath:~-1%==\ SET datapath=%datapath:~0,-1%
</pre>