#!/bin/bash

APP_HOME="`dirname "$0"`"
DOCKER_SCRIPTS_COMMON="$APP_HOME""/../common.sh"

source $DOCKER_SCRIPTS_COMMON

docker stop $DOCKER_CONTAINER_CENTOS7_BUILDER_GRADLE_NAME && echo "1" || echo "0"

