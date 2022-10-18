@echo off

cd %~dp0

set TARGET=..\generated\testcalendar_internals.md
set SOURCE=..\gen_sources

type noedit.htm > %TARGET%
type %SOURCE%\basic_ham.md >> %TARGET%
type %SOURCE%\basic_filters.md >> %TARGET%
type %SOURCE%\basic_rewrite.md >> %TARGET%
type %SOURCE%\basic_http_s.md >> %TARGET%
type %SOURCE%\basic_recording.md >> %TARGET%
type %SOURCE%\basic_replaying.md >> %TARGET%
type %SOURCE%\basic_proxy.md >> %TARGET%