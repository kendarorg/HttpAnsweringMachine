@ECHO OFF
SET mypath=%~dp0
SET mypathx=%~dp0
cd %mypath%

set /p builddocker="Build docker images (y/N): "
if "%builddocker%"=="n" goto requery2
if "%builddocker%"=="y" goto requery2
if "%builddocker%"=="N" (
	set builddocker=n
	goto requery2
)
if "%builddocker%"=="Y" (
	set builddocker=y
	goto requery2
)
set builddocker=n

:requery2
set /p mavenbuild="Build java packages (y/N): "
if "%mavenbuild%"=="n" goto ok
if "%mavenbuild%"=="y" goto ok
if "%mavenbuild%"=="N" (
	set mavenbuild=n
	goto ok
)
if "%mavenbuild%"=="Y" (
	set mavenbuild=y
	goto ok
)
set mavenbuild=n


:ok

if "%mavenbuild%"=="n" goto buildocker
echo Building HAM
cd ham
call mvn clean install
cd ..

echo Building sample applications
cd samples\sampleapp
call mvn clean install

cd ..
cd ..


:buildocker
if "%builddocker%"=="n" goto end

echo Building main docker images
cd %mypath%docker\images
call ImagesBuild.bat

echo Building sampleapp docker images
cd %mypathx%samples\sampleapp\docker_multi
call ImagesBuild.bat

cd %mypathx%



:end

pause
