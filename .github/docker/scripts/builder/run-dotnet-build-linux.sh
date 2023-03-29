#!/bin/bash

### This script expects the desired repo to already be checked out
### and mounted to /repo.

REPO_DIR="/repo"
HIRS_DIR="/HIRS"
HIRS_DOTNET_DIR="/HIRS_Provisioner.NET/hirs"
OUTPUT_DIR="/output"

### Assume the mounted repo dir is read-only.
if [ ! -d "$REPO_DIR" ]; then
    echo "No repository mounted to $REPO_DIR."
    exit 1
fi

if [ ! -d "$OUTPUT_DIR" ]; then
    echo "No output directory mounted to $OUTPUT_DIR."
    exit 1
fi

cd "$REPO_DIR"
mkdir -p "$HIRS_DIR"

cp -r . "$HIRS_DIR"

cd "$HIRS_DIR""$HIRS_DOTNET_DIR"

echo "Beginning Build."
if dotnet publish -r linux-x64 -c Release --self-contained ; then
    echo "Build successful."
else
    echo "Build failed."
    exit 1
fi

