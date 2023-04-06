#!/bin/bash

####### These scripts assume a file structure within the repo:
## HIRS/
##   .ci/
##     docker/
##      scripts/
##       builder/
##       common.sh
##       control/
#######
DOCKER_SCRIPTS_HOME=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd ) 

BUILDER_REPO_DIR="/repo"
BUILDER_WORKING_REPO_DIR="/HIRS"
BUILDER_OUTPUT_DIR="/output"


DOCKER_CONTAINER_CENTOS7_BUILDER_DOTNET_NAME="centos7-builder-dotnet"
DOCKER_CONTAINER_CENTOS7_BUILDER_GRADLE_NAME="centos7-builder-gradle"

GITHUB_IMAGE_CENTOS7_BUILDER="ghcr.io/nsacyber/hirs/centos7-builder" 

HOST_OUTPUT_DIR="/tmp/hirs-builders/"
HOST_CENTOS7_OUTPUT_DIR="$HOST_OUTPUT_DIR""centos7/"

HIRS_RELATIVE_ROOT_DIR="$DOCKER_SCRIPTS_HOME""/../../../"
HIRS_DOCKER_SCRIPTS_BUILDER_DIR="$DOCKER_SCRIPTS_HOME""/builder/"
HIRS_DOCKER_SCRIPTS_CONTROL_DIR="$DOCKER_SCRIPTS_HOME""/control/"


