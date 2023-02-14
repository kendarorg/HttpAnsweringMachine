## VErsion

### Main

The version is like 4.1.1 or 5.1.1-SNAPSHOT

* Seek if there are occurrencies of <version>OLD</version> for other than kendar packages
* Seek if there are occurrencies of <version>NEW</version> .. jump the version if it's the case
* Replace <version>OLD</version>  with <version>NEW</version>
* Replace <ham.version>OLD</ham.version> with <ham.version>NEW</ham.version>
* Replace HAM_VERSION=OLD with HAM_VERSION=NEW
* Replace version-OLD with version-NEW
* Change the content of "scripts/version.txt" with the new version (NO LINE FEEDS!!)

### Janus

The version is like 4.1.1 or 5.1.1-SNAPSHOT

* Replace JANUS_DRIVER_VERSION=OLD to JANUS_DRIVER_VERSION=NEW
* Replace <janus.version>OLD</janus.version> with <janus.version>NEW</janus.version>

## DEPLOY

set DOCKER_IP=192.168.56.2
set DOCKER_HOST=tcp://%DOCKER_IP%:23750
set DOCKER_DEPLOY=true

Set-Variable -Name "DOCKER_IP" -Value "192.168.56.2" -Scope global
Set-Variable -Name "DOCKER_HOST" -Value "tcp://192.168.56.2:32750" -Scope global
Set-Variable -Name "DOCKER_DEPLY" -Value "true" -Scope global

export DOCKER_DEPLOY=true
# The docker on which should firstly deploy
export DOCKER_HOST=tcp://192.168.1.40:23750
./scripts/build/build_release.sh
./scripts/build/build_release_samples.sh
./scripts/build/build_docker.sh  TWICE....
./scripts/build/build_docker_samples.sh  TWICE....
./scripts/build/deploy_jar.sh
Upload on github the releases on release dir


## TEST

unset DOCKER_DEPLOY
# The docker on which should firstly deploy
export DOCKER_IP=192.168.1.40
export DOCKER_HOST=tcp://$DOCKER_IP:23750
./globaltest.sh
Upload on github the releases on release dir


## CLeanup

docker rmi $(docker images | grep "<none>"|grep -v "kendar" | awk "{print $3}")
