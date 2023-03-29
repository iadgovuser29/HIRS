#!/bin/bash

APP_HOME="`dirname "$0"`"
DOCKER_SCRIPTS_COMMON="$APP_HOME""/common.sh"

docker exec -it $DOCKER_CONTAINER_CENTOS7_BUILDER_GRADLE_NAME /bin/bash -c "$HIRS_DOCKER_SCRIPTS_BUILDER_DIR/package-centos.sh" && echo "1" || echo "0"
