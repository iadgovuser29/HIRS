#!/bin/bash
set -x # echo commands as they are executed

APP_HOME=$(realpath "`dirname "$0"`")
DOCKER_SCRIPTS_COMMON="$APP_HOME""/../common.sh"

source $DOCKER_SCRIPTS_COMMON

docker exec -dit $DOCKER_CONTAINER_CENTOS7_BUILDER_GRADLE_NAME /bin/bash -c "cp $HIRS_RELATIVE_ROOT_DIR/ -iname \"*log\" $BUILDER_OUTPUT_DIR" && echo "1" || echo "0"

