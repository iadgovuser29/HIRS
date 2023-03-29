#!/bin/bash

APP_HOME="`dirname "$0"`"
SETUP_SCRIPT="$APP_HOME""/setup-builder.sh"

source $SETUP_SCRIPT

echo "Beginning Build & Unit Test."
if ./gradlew clean build ; then
    echo "Build & Unit Test successful."
else
    echo "Build & Unit Test failed."
    exit 1
fi

