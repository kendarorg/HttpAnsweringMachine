@echo off

cd %~dp0

set TARGET=..\generated\quickstart.md
set SOURCE=..\gen_sources

type noedit.htm > %TARGET%
echo In this demo you will >> %TARGET%
type quickstart.md >> %TARGET%
type %SOURCE%\quickinstall.md >> %TARGET%
type %SOURCE%\proxy.md >> %TARGET%