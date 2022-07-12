#!/bin/bash

sudo mkdir -p /mnt/hirs-ci/{builds,logs,repos}
sudo chmod -R 777 /mnt/hirs-ci

mv HIRS /mnt/hirs-ci/repos

cd /mnt/hirs-ci/repos/HIRS/.ci/compose
docker compose -f ./compose-hirs-ci.yml up -d
echo "Letting docker compose finish..."
sleep 30
docker exec compose-aca-centos7-1 sh -c 'sh $HIRS_CI_REPOS_HIRS/.ci/compose/wait-aca.sh aca-centos7'

sh ./run-test-on-container.sh dotnet-rocky85-runonly aca-centos7 ../..
retVal=$?
if [ $retVal -ne 0 ]; then
    echo "Stopping after test on dotnet-rocky85-runonly"
    exit 1
fi

sh ./run-test-on-container.sh dotnet-ubuntu20-runonly aca-centos7 ../..
retVal=$?
if [ $retVal -ne 0 ]; then
    echo "Stopping after test on dotnet-ubuntu20-runonly"
    exit 1
fi

sh ./run-test-on-container.sh dotnet-ubuntu20-builder aca-centos7 ../..
retVal=$?
if [ $retVal -ne 0 ]; then
    echo "Stopping after test on dotnet-ubuntu20-builder"
    exit 1
fi