@echo off

cd %~dp0

set TARGET=..\generated\android.md
set SOURCE=..\gen_sources

type noedit.htm > %TARGET%
echo In this demo you will >> %TARGET%
type android.md >> %TARGET%
type %SOURCE%\quickinstall.md >> %TARGET%