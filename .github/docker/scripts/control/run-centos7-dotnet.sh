#!/bin/bash
set -x # echo commands as they are executed

APP_HOME="`dirname "$0"`"
DOCKER_SCRIPTS_COMMON="$APP_HOME""/../common.sh"

source $DOCKER_SCRIPTS_COMMON

docker run -dit -v "$HOST_CENTOS7_OUTPUT_DIR":"$BUILDER_OUTPUT_DIR" -v "$HIRS_RELATIVE_ROOT_DIR":"$BUILDER_REPO_DIR":ro --name="$DOCKER_CONTAINER_CENTOS7_BUILDER_DOTNET_NAME" $GITHUB_IMAGE_CENTOS7_BUILDER /bin/bash -c "$BUILDER_REPO_DIR/.github/docker/scripts/builder/setup-builder.sh && tail -f /dev/null"

# Wait for builder to be ready
until [ "`docker exec -i $DOCKER_CONTAINER_CENTOS7_BUILDER_DOTNET_NAME ls $BUILDER_WORKING_REPO_DIR`" ]; do
    sleep 5;
done;
echo "$DOCKER_CONTAINER_CENTOS7_BUILDER_DOTNET_NAME ready."

