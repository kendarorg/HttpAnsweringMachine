@echo off

cd %~dp0

set TARGET=..\generated
set SOURCE=..\gen_sources

echo In this demo you will > %TARGET%\localsample.md
type localsample.md >> %TARGET%\localsample.md
type %SOURCE%\quickinstall.md >> %TARGET%\localsample.md
type %SOURCE%\proxy.md >> %TARGET%\localsample.md
type %SOURCE%\recordcalendar.md >> %TARGET%\localsample.md
type %SOURCE%\installcertificate.md >> %TARGET%\localsample.md
type %SOURCE%\interceptgoogle.md >> %TARGET%\localsample.md
type %SOURCE%\bingifygoogle.md >> %TARGET%\localsample.md