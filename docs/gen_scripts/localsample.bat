@echo off

cd %~dp0

set TARGET=..\generated\localsample.md
set SOURCE=..\gen_sources

type noedit.htm > %TARGET%
echo In this demo you will >> %TARGET%
type localsample.md >> %TARGET%
type %SOURCE%\quickinstall.md >> %TARGET%
type %SOURCE%\samplestructure.md >> %TARGET%
type %SOURCE%\proxy.md >> %TARGET%
type %SOURCE%\recordcalendar.md >> %TARGET%