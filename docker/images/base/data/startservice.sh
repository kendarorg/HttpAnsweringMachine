#!/bin/bash

rootAppDir=~/test/etc/app
rootServicesDir=~/test/etc/services

mkdir -p $rootAppDir
mkdir -p $rootServicesDir

captureLogs="false"
isConfig="false"
appDir="unknown"
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
done

if [[ "$appDir" == "unknown" ]]; then
  echo ""
  echo "startservice.sh params:"
  echo "  --app=APPDIR, mandatory, run ${rootAppDir}/APPDIR/APPDIR.sh file"
  echo "  --capturelogs, optional, write the logs in ${rootAppDir}//APPDIR/logs"
  echo "    instead of stdout"
  echo "  --config, optional, execute once then sleep infinity"
  echo ""
  echo "Real scripts will be written in ${rootServicesDir}/APPDIR/run file"
  exit 1
fi

echo Application Director $rootAppDir/$appDir

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
echo "exec ${rootAppDir}/${appDir}/${appDir}.sh" >> $rootServicesDir/$appDir/run
if [[ "$isConfig" == "true" ]]; then     ## GOOD
  echo "sleep infinity" >> $rootServicesDir/$appDir/run
fi

chmod +x "${rootServicesDir}/${appDir}/run"
#chmod +x "${rootAppDir}/${appDir}/${appDir}.sh"

if [ "$captureLogs" == "true" ]; then     ## GOOD
  mkdir -p ${rootAppDir}/${appDir}/log
  mkdir -p ${rootServicesDir}/${appDir}/log
  echo '#!/bin/bash\nexec svlogd -tt ${rootAppDir}/${appDir}/log\n' > $rootServicesDir/$appDir/log/run
  chmod +x ${rootServicesDir}/${appDir}/log/run
fi

#RUN mkdir -p /etc/service/${appDir} \
#    && mkdir -p /etc/app/${appDir}/log \
#    && mkdir -p /etc/service/${appDir}/log \
#    && echo -e "#!/bin/bash\nexec svlogd -tt /etc/app/${appDir}/log\n" > /etc/service/${appDir}/log/run \
#    && chmod +x /etc/service/${appDir}/log/run \
#    && echo -e "#!/bin/bash\nexec 2>&1\nexec /etc/app/${appDir}/${appDir}.sh\n" > /etc/service/${appDir}/run \
#    && chmod +x /etc/service/${appDir}/run