#!/bin/bash

APP_HOME="`dirname "$0"`"
DOCKER_SCRIPTS_COMMON="$APP_HOME""/common.sh"

source $DOCKER_SCRIPTS_COMMON

# See the common script for assume file structure of the repo.
docker run -dit -v "$HOST_CENTOS7_OUTPUT_DIR":"$BUILDER_OUTPUT_DIR" -v "$HIRS_RELATIVE_ROOT_DIR":"$BUILDER_REPO_DIR":ro --name="$DOCKER_CONTAINER_CENTOS7_BUILDER_GRADLE_NAME" $GITHUB_IMAGE_CENTOS7_BUILDER /bin/bash -c "$BUILDER_REPO_DIR/.ci/docker/scripts/builder/setup-builder.sh && tail -f /dev/null" && echo "1" || echo "0"

