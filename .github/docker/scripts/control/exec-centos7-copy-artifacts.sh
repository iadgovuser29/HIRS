#!/bin/bash
set -x # echo commands as they are executed

APP_HOME=$(realpath "`dirname "$0"`")
DOCKER_SCRIPTS_COMMON="$APP_HOME""/../common.sh"

source $DOCKER_SCRIPTS_COMMON

docker exec -dit $DOCKER_CONTAINER_CENTOS7_BUILDER_GRADLE_NAME /bin/bash -c "find $HIRS_RELATIVE_ROOT_DIR/package/rpm/RPMS/ -iname \"*el7*rpm\" $BUILDER_OUTPUT_DIR" && echo "1" || echo "0"

