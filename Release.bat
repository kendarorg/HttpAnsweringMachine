@echo off
set VERSION=3.0.7
call Make.bat

SET mypath=%~dp0
cd %mypath%


del /q /s docker\images\client\data\app >NUL 2>&1
rmdir /q /s docker\images\client\data\app >NUL 2>&1
del /q docker\images\client\data\*.jar >NUL 2>&1
del /q /s docker\images\master\data\app >NUL 2>&1
rmdir /q /s docker\images\master\data\app >NUL 2>&1

del /q /s release\target >NUL 2>&1
rmdir /q /s release\target >NUL 2>&1
md release\target >NUL 2>&1
md release\target\libs >NUL 2>&1
md release\target\external >NUL 2>&1
md release\target\docker >NUL 2>&1


echo Preparing Jars
copy ham\app\target\app-%VERSION%.jar release\target\ 1>NUL
copy ham\simpledns\target\simpledns-%VERSION%.jar release\target\ 1>NUL
copy ham\libs\*.jar release\target\libs\ 1>NUL
copy ham\external\*.* release\target\external\ 1>NUL
copy ham\external.json release\target\ 1>NUL
echo Preparing Docker
xcopy /e /k /h /i /y docker\images release\target\docker 1>NUL
del /q release\target\docker\*.bat >NUL 2>&1
del /q release\target\docker\*.sh >NUL 2>&1

del /q /s release\target\docker\externalvpn >NUL 2>&1
rmdir /q /s release\target\docker\externalvpn >NUL 2>&1
copy release\ImagesBuild.* release\target\docker 1>NUL
copy release\Run.* release\target 1>NUL
