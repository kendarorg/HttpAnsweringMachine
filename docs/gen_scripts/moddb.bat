@echo off

cd %~dp0

set TARGET=..\generated\moddb.md
set SOURCE=..\gen_sources

type noedit.htm > %TARGET%
echo In this demo you will >> %TARGET%
type moddb.md >> %TARGET%
type %SOURCE%\quickinstalldb.md >> %TARGET%
type %SOURCE%\samplestructure.md >> %TARGET%
type %SOURCE%\proxy.md >> %TARGET%
type %SOURCE%\recordcalendardbsimple.md >> %TARGET%
type %SOURCE%\replaymoddb.md >> %TARGET%