#!/bin/bash

APP_HOME="`dirname "$0"`"
DOCKER_SCRIPTS_COMMON="$APP_HOME""/common.sh"

source $DOCKER_SCRIPTS_COMMON

chmod +x "$HIRS_DOCKER_SCRIPTS_BUILDER_DIR/*.sh"

