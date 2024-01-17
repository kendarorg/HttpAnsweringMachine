@ECHO OFF

set HAM_VERSION=4.3.1
REM Initialize
set START_DIR=%~dp0
cd %START_DIR%
cd ..
cd ..
cd ham
set SCRIPT_DIR=%cd%

REM Retrieve the jar name
cd jacoco
mvn test
move "%cd%\target\coverage-report" "%START_DIR%\release\"