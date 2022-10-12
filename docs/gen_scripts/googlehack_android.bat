@echo off

cd %~dp0

set TARGET=..\generated\googlehack_android.md
set SOURCE=..\gen_sources

type noedit.htm > %TARGET%
echo In this demo you will >> %TARGET%
type googlehack_android.md >> %TARGET%
type %SOURCE%\quickinstalllocal.md >> %TARGET%
type %SOURCE%\installcertificate_android.md >> %TARGET%
type %SOURCE%\proxy_android.md >> %TARGET%
type %SOURCE%\intercept_android.md >> %TARGET%
type %SOURCE%\interceptgoogle.md >> %TARGET%
type %SOURCE%\bingifygoogle.md >> %TARGET%