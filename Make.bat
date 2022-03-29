@ECHO OFF
SET mypath=%~dp0
cd %mypath%

set /p builddocker="Build docker images (y/N): "
if "%builddocker%"=="n" goto requery2
if "%builddocker%"=="y" goto requery2
if "%builddocker%"=="N" (
	builddocker=n
	goto requery2
)
if "%builddocker%"=="Y" (
	builddocker=y
	goto requery2
)
builddocker=n

:requery2
set /p mavenbuild="Build java packages (y/N): "
if "%mavenbuild%"=="n" goto ok
if "%mavenbuild%"=="y" goto ok
if "%mavenbuild%"=="N" (
	mavenbuild=n
	goto ok
)
if "%mavenbuild%"=="Y" (
	mavenbuild=y
	goto ok
)
mavenbuild=n


:ok

if "%mavenbuild%"=="n" goto buildocker
echo Building HAM
cd ham
call mvn clean install
cd ..
pause
echo Building sample applications
cd samples\sampleapp
call mvn clean install
pause

:buildocker
if "%builddocker%"=="n" goto end

echo Building main docker images
cd %mypath%docker\images
call ImagesBuild.bat

echo Building sampleapp docker images
cd %mypath%samples\sampleapp\docker_multi
call ImagesBuild.bat



:end

pause