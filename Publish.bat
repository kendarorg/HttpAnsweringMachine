@echo off

call Make.bat

SET mypath=%~dp0
cd %mypath%

cd  %mypath%\ham
mvn deploy