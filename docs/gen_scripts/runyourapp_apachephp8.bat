@echo off

cd %~dp0

set TARGET=..\generated\runyourapp_apachephp8.md
set SOURCE=..\gen_sources

type noedit.htm > %TARGET%
echo In this demo you will >> %TARGET%
type runyourapp_apachephp8.md >> %TARGET%
type %SOURCE%\runyourapp_index.md >> %TARGET%
type %SOURCE%\runyourapp_master.md >> %TARGET%
type %SOURCE%\runyourapp_target_apachephp8.md >> %TARGET%
type %SOURCE%\runyourapp_compose.md >> %TARGET%
type %SOURCE%\runyourapp_vpn.md >> %TARGET%
type %SOURCE%\runyourapp_next.md >> %TARGET%