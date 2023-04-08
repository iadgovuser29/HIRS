#!/bin/bash
set -x # echo commands as they are executed

APP_HOME=$(realpath "`dirname "$0"`")
DOCKER_SCRIPTS_COMMON="$APP_HOME""/../common.sh"

source $DOCKER_SCRIPTS_COMMON

docker exec -i $DOCKER_CONTAINER_CENTOS7_BUILDER_DOTNET_NAME "find" "'$BUILDER_WORKING_REPO_DIR/HIRS_Provisioner.NET'" "-iname" "\"*log\"" "-exec" "cp" "{}" "'$BUILDER_OUTPUT_DIR'" "\;"
