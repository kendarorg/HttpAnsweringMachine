#!/bin/bash

captureLogs="false"
isConfig=""
appDir=$1
if [ -z "$2" ]; then
    captureLogs=$2
fi

if [ -z "$3" ]; then
    isConfig="sleep infinity\n"
fi

#setup simple run
mkdir -p /etc/service/${appDir}
echo -e "#!/bin/bash\nexec 2>&1\nexec /etc/app/${appDir}/${appDir}.sh\n${isConfig}" > /etc/service/${appDir}/run
chmod +x /etc/service/${appDir}/run

if [ "$captureLogs" == "true" ]; then     ## GOOD
  mkdir -p /etc/app/${appDir}/log
  mkdir -p /etc/service/${appDir}/log
  echo -e "#!/bin/bash\nexec svlogd -tt /etc/app/${appDir}/log\n" > /etc/service/${appDir}/log/run
  chmod +x /etc/service/${appDir}/log/run
fi

#RUN mkdir -p /etc/service/${appDir} \
#    && mkdir -p /etc/app/${appDir}/log \
#    && mkdir -p /etc/service/${appDir}/log \
#    && echo -e "#!/bin/bash\nexec svlogd -tt /etc/app/${appDir}/log\n" > /etc/service/${appDir}/log/run \
#    && chmod +x /etc/service/${appDir}/log/run \
#    && echo -e "#!/bin/bash\nexec 2>&1\nexec /etc/app/${appDir}/${appDir}.sh\n" > /etc/service/${appDir}/run \
#    && chmod +x /etc/service/${appDir}/run