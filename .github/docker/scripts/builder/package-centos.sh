#!/bin/bash

APP_HOME="`dirname "$0"`"
SETUP_SCRIPT="$APP_HOME""/setup-builder.sh"

source $SETUP_SCRIPT

cd $BUILDER_WORKING_REPO_DIR

echo "Beginning HIRS CentOS packaging script."
if ./package/package.centos.sh ; then
    echo "Packaging succesful."
    cp ./package/rpm/RPMS/noarch/*.rpm $BUILDER_OUTPUT_DIR/
    cp ./package/rpm/RPMS/x86_64/*.rpm $BUILDER_OUTPUT_DIR/
else
    echo "Packaging not successful."
    exit 1
fi

