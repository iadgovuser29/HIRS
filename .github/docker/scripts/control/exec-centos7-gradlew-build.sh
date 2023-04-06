#!/bin/bash

APP_HOME=$(realpath "`dirname "$0"`")
DOCKER_SCRIPTS_COMMON="$APP_HOME""/../common.sh"

source $DOCKER_SCRIPTS_COMMON

cmd="'$BUILDER_WORKING_REPO_DIR/.github/docker/scripts/builder/gradlew-build.sh'"

docker exec $DOCKER_CONTAINER_CENTOS7_BUILDER_GRADLE_NAME //bin/bash -c $cmd && echo "1"

