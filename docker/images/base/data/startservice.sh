#!/bin/bash

rootAppDir=/etc/app
rootServicesDir=/etc/service

mkdir -p $rootAppDir
mkdir -p $rootServicesDir

captureLogs="false"
isConfig="false"
appDir="unknown"
runner="unknown"
runnerSet="false"
for arg in "$@"
do
    if [[ "$arg" == "--capturelogs" ]]; then
      captureLogs="true"
    fi
    if [[ "$arg" == "--config" ]]; then
      isConfig="true"
    fi
    if [[ $arg == --app=* ]]; then
      appDir=${arg:6}
    fi
    if [[ $arg == --run=* ]]; then
      runner=${arg:6}
	  runnerSet="true"
    fi
done

if [[ "$appDir" == "unknown" ]]; then
  echo ""
  echo "startservice.sh params:"
  echo "  --app=APPDIR, mandatory, run ${rootAppDir}/APPDIR/APPDIR.sh file"
  echo "  --run=APPSH, optional, sets the full path of the executable to run"
  echo "  --capturelogs, optional, write the logs in ${rootAppDir}/APPDIR/logs"
  echo "    instead of stdout"
  echo "  --config, optional, execute once then sleep infinity"
  echo ""
  echo "Real scripts will be written in ${rootServicesDir}/APPDIR/run file"
  exit 1
fi



echo Application Director $rootAppDir/$appDir

if [[ $runnerSet == true ]]; then
  echo Set alternative runner path $runner
else
  runner = $rootAppDir/$appDir/$appDir.sh
fi
if [[ $captureLogs == true ]]; then
  echo Capture Logs on $rootAppDir/$appDir/log
fi
if [[ $isConfig == true ]]; then
  echo This is a Config script
else
  echo This is a Service script
fi

doSleepInfinty=""


#setup simple run
mkdir -p "${rootAppDir}/${appDir}"
mkdir -p "${rootServicesDir}/${appDir}"
echo '#!/bin/bash' > $rootServicesDir/$appDir/run
echo "exec 2>&1" >> $rootServicesDir/$appDir/run
echo "exec ${runner}" >> $rootServicesDir/$appDir/run
if [[ "$isConfig" == "true" ]]; then     ## GOOD
  echo "/etc/DoSleep.sh" >> $rootServicesDir/$appDir/run
fi

chmod +x "${rootServicesDir}/${appDir}/run"
if [ -f "${runner}" ]; then
  chmod +x "${runner}"
fi
#chmod +x "${rootAppDir}/${appDir}/${appDir}.sh"

if [ "$captureLogs" == "true" ]; then     ## GOOD
  mkdir -p "${rootAppDir}/${appDir}/log"
  mkdir -p "${rootServicesDir}/${appDir}/log"
  
  echo '#!/bin/bash' > $rootServicesDir/$appDir/log/run
  echo "exec svlogd -tt ${rootAppDir}/${appDir}/log" >> $rootServicesDir/$appDir/log/run
  chmod +x "${rootServicesDir}/${appDir}/log/run"
fi
