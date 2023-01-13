@echo off

cd %~dp0

set TARGET=..\generated\autodbtest.md
set SOURCE=..\gen_sources

type noedit.htm > %TARGET%
echo In this demo you will >> %TARGET%
type autodbtest.md >> %TARGET%
type %SOURCE%\quickinstalldb.md >> %TARGET%
type %SOURCE%\samplestructure.md >> %TARGET%
type %SOURCE%\proxy.md >> %TARGET%
type %SOURCE%\recordcalendardb.md >> %TARGET%
type %SOURCE%\autoreplaycalendardb.md >> %TARGET%