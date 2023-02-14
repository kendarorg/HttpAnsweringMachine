#!/bin/bash

CALENDAR_PATH=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd $CALENDAR_PATH

java -cp $CALENDAR_PATH/h2-2.1.214.jar org.h2.tools.Server -web -ifNotExists -tcpPort 9123 -tcp -webAllowOthers -tcpAllowOthers -tcpPassword sa
