@echo off

cd %~dp0

set TARGET=..\generated\googlehack.md
set SOURCE=..\gen_sources

type noedit.htm > %TARGET%
echo In this demo you will >> %TARGET%
type googlehack.md >> %TARGET%
type %SOURCE%\quickinstalllocal.md >> %TARGET%
type %SOURCE%\installcertificate.md >> %TARGET%
type %SOURCE%\interceptgoogle.md >> %TARGET%
type %SOURCE%\bingifygoogle.md >> %TARGET%