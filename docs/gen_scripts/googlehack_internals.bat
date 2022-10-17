@echo off

cd %~dp0

set TARGET=..\generated\googlehack_internals.md
set SOURCE=..\gen_sources

type noedit.htm > %TARGET%
type %SOURCE%\basic_ham.md >> %TARGET%
type %SOURCE%\basic_dns.md >> %TARGET%
type %SOURCE%\basic_filters.md >> %TARGET%
type %SOURCE%\basic_http_s.md >> %TARGET%
type %SOURCE%\basic_proxy.md >> %TARGET%