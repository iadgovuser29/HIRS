#!/bin/bash
set -x # echo commands as they are executed

REPO_DIR="/repo"
WORKING_REPO_DIR="/HIRS"
OUTPUT_DIR="/output"

# Argument handling https://stackoverflow.com/questions/192249/how-do-i-parse-command-line-arguments-in-bash
POSITIONAL_ARGS=()

while [[ $# -gt 0 ]]; do
  case $1 in
    --in-place)
      INPLACE_ARG=YES
      shift # past argument
      ;;
    -*|--*)
      echo "setup-builder.sh: Unknown option $1"
      ;;
    *)
      POSITIONAL_ARGS+=("$1") # save positional arg
      shift # past argument
      ;;
  esac
done

set -- "${POSITIONAL_ARGS[@]}" # restore positional parameters


if [ -n "${INPLACE_ARG}" ]; then
    # Make sure the repo is mounted to the working dir
    if [ ! -d "$WORKING_REPO_DIR" ]; then
        echo "No repository found mounted to $WORKING_REPO_DIR."
	exit 2
    fi
else
    ### Assume the mounted repo dir is read-only.
    if [ ! -d "$REPO_DIR" ]; then
        echo "No repository mounted to $REPO_DIR."
	exit 3
    fi

    # Copy the mounted folder so that it doesn't affect the host
    cd "$REPO_DIR"
    mkdir -p "$WORKING_REPO_DIR"
    cp -r . "$WORKING_REPO_DIR"
fi

if [ ! -d "$OUTPUT_DIR" ]; then
    echo "No output directory mounted to $OUTPUT_DIR."
    exit 4
fi

cd "$WORKING_REPO_DIR"




