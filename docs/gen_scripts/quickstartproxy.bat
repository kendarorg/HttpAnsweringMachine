@echo off

cd %~dp0

set TARGET=..\generated\quickstartproxy.md
set SOURCE=..\gen_sources

type noedit.htm > %TARGET%
echo In this demo you will >> %TARGET%
type quickstartproxy.md >> %TARGET%
type %SOURCE%\proxy.md >> %TARGET%
type %SOURCE%\approxy.md >> %TARGET%
type %SOURCE%\runproxyham.md >> %TARGET%
type %SOURCE%\createrecording.md >> %TARGET%