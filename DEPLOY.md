## DEPLOY

export DOCKER_DEPLOY=true
# The docker on which should firstly deploy
export DOCKER_HOST=tcp://192.168.1.40:23750
./scripts/build/build_release.sh
./scripts/build/build_release_samples.sh
./scripts/build/build_docker.sh
./scripts/build/build_docker_samples.sh
./scripts/build/deploy_jar.sh
Upload on github the releases on release dir

## TEST

unset DOCKER_DEPLOY
# The docker on which should firstly deploy
export DOCKER_IP=192.168.1.40
export DOCKER_HOST=tcp://$DOCKER_IP:23750
./globaltest.sh
Upload on github the releases on release dir
