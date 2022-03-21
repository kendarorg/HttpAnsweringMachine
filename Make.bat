@ECHO OFF
SET mypath=%~dp0
cd %mypath%

:requery
set /p builddocker="Build docker images (y/n): "
if "%builddocker%"=="n" goto ok
if "%builddocker%"=="y" goto ok
if "%builddocker%"=="N" (
	builddocker=n
	goto ok
)
if "%builddocker%"=="Y" (
	builddocker=y
	goto ok
)
goto requery


:ok

echo Building HAM
cd ham
call mvn clean install
cd ..
pause
echo Building sample applications
cd samples\sampleapp
call mvn clean install
pause


if "%builddocker%"=="n" goto end

echo Building main docker images
cd %mypath%docker\images
call ImagesBuild.bat

echo Building sampleapp docker images
cd %mypath%samples\sampleapp\docker_multi
call ImagesBuild.bat



:end

pause