#!/bin/bash
set -x # echo commands as they are executed

APP_HOME=$(realpath "`dirname "$0"`")
DOCKER_SCRIPTS_COMMON="$APP_HOME""/../common.sh"

source $DOCKER_SCRIPTS_COMMON

docker exec -i $DOCKER_CONTAINER_CENTOS7_BUILDER_GRADLE_NAME /bin/bash -c "\"$BUILDER_WORKING_REPO_DIR/.github/docker/scripts/builder/gradlew-build.sh\"" && echo "1"

