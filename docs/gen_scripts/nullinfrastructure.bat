@echo off

cd %~dp0

set TARGET=..\generated
set SOURCE=..\gen_sources

echo In this demo you will > %TARGET%\nullinfrastracture.md
type nullinfrastracture.md >> %TARGET%\nullinfrastracture.md
type %SOURCE%\quickinstall.md >> %TARGET%\nullinfrastracture.md
type %SOURCE%\proxy.md >> %TARGET%\localsample.md
type %SOURCE%\recordcalendar.md >> %TARGET%\localsample.md