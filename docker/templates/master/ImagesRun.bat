
REM TODO Set the application name
SET YOUR_APP_NAME=test
SET YOUR_APP_FULL_PATH=C:\Users\username\test\target

set /p builddocker="Build docker image (y/N): "
if "%builddocker%"=="n" goto go
if "%builddocker%"=="y" goto go
if "%builddocker%"=="N" (
	builddocker=n
	goto go
)
if "%builddocker%"=="Y" (
	builddocker=y
	goto go
)
builddocker=n

:go

if "%builddocker%"=="n" goto rundocker
docker build --rm -t app.%YOUR_APP_NAME% .

:rundocker

docker run --name %YOUR_APP_NAME% --privileged ^
	--cap-add SYS_ADMIN --cap-add DAC_READ_SEARCH ^
	-v "%YOUR_APP_FULL_PATH%" /app ^
	--dns=127.0.0.1 app.%YOUR_APP_NAME%
