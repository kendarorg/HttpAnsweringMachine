#!/bin/bash

export DOCKER_USERNAME=none
export DOCKER_LOGIN=none
export DOCKER_TOKEN=none
export DOCKER_ORG=none

function docker_login {
  if [ "$DOCKER_DEPLOY" == "true" ]; then
    DOCKER_USERNAME=$1
    DOCKER_PASSWORD=$2
    DOCKER_ORG=$3
    docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD
    DOCKER_TOKEN=`curl -s -H "Content-Type: application/json" \
      -X POST -d "$(_docker_login_data)" "https://hub.docker.com/v2/users/login/" | jq -r .token`
    DOCKER_PASSWORD=none
  fi
}

function docker_logout {
    DOCKER_PASSWORD=none
    DOCKER_TOKEN=none
}

function _docker_login_data {
cat <<EOF
{
  "username": "$DOCKER_USERNAME",
  "password": "$DOCKER_USERNAME"
}
EOF
}

function docker_remove_tag {
  echo -n ""
  # IMAGE_NAME=$1
  # TAG=$2
  # curl "https://hub.docker.com/v2/repositories/${DOCKER_ORG}/${IMAGE_NAME}/tags/${TAG}/" \
  #   -X DELETE \
  #   -H "Authorization: JWT ${DOCKER_TOKEN}"
}

function docker_push {
  if [ "$DOCKER_DEPLOY" == "true" ]; then
    IMAGE_NAME=$1
    VERSION_NUMBER=$2

    if [[ "$VERSION_NUMBER" == *"snapshot"* ]] ;then
      echo Removing tag $IMAGE_NAME
      docker_remove_tag "$IMAGE_NAME" snapshot
      docker_remove_tag "$IMAGE_NAME" "$VERSION_NUMBER"
      echo Pushing image tag $IMAGE_NAME
      docker push $DOCKER_ORG/%IMAGE_NAME%:v%VERSION_NUMBER%
      docker push $DOCKER_ORG/%IMAGE_NAME%:snapshot
      echo Tagging image $IMAGE_NAME
      docker tag %IMAGE_NAME% $DOCKER_ORG/%IMAGE_NAME%:v%VERSION_NUMBER%
      docker tag $DOCKER_ORG/%IMAGE_NAME%:v%VERSION_NUMBER% $DOCKER_ORG/%IMAGE_NAME%:snapshot
    else
      echo Pushing image tag $IMAGE_NAME
      docker push $DOCKER_ORG/$IMAGE_NAME:v$VERSION_NUMBER
      docker push $DOCKER_ORG/$IMAGE_NAME:latest
      docker push $DOCKER_ORG/$IMAGE_NAME:snapshot
      echo Tagging image $IMAGE_NAME
      docker tag $IMAGE_NAME $DOCKER_ORG/$IMAGE_NAME:v$VERSION_NUMBER
      docker tag $DOCKER_ORG/$IMAGE_NAME:v$VERSION_NUMBER $DOCKER_ORG/$IMAGE_NAME:latest
      docker tag $DOCKER_ORG/$IMAGE_NAME:v$VERSION_NUMBER $DOCKER_ORG/$IMAGE_NAME:snapshot
    fi
  fi
}