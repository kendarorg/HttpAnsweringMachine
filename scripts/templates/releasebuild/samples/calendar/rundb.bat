@echo off
set CALENDAR_PATH=%~dp0
cd %CALENDAR_PATH%

call java -cp %CALENDAR_PATH%h2-2.1.214.jar org.h2.tools.Server -web -ifNotExists -tcpPort 9123 -tcp -webAllowOthers -tcpAllowOthers -tcpPassword sa
