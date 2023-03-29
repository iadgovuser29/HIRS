#!/bin/bash

APP_HOME="`dirname "$0"`"
DOCKER_SCRIPTS_COMMON="$APP_HOME""/../common.sh"

source $DOCKER_SCRIPTS_COMMON

if [ $# -eq 0 ]
  then
    echo "exec- No arguments supplied"
fi


