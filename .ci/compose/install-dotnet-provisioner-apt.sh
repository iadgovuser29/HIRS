#!/bin/bash

filename=$HIRS_CI_BUILDS_DOTNET_PROVISIONER_DEB

apt-get install -y sudo

echo "Waiting for $filename to finish building ..."
until [ -f $filename ]; do
  sleep 5;
done

echo "Installing provisioner"
sudo apt-get install -y $filename || true
echo "::warning ::deb install needs attention"

echo "Copying appsettings"
sudo cp $HIRS_CI_REPOS_HIRS/.ci/setup/appsettings.json /usr/share/hirs/
echo "Configuring provisioner to point to $1"
sed -i "s/127.0.0.1/aca-centos7/g" /usr/share/hirs/appsettings.json

sudo cp $HIRS_CI_REPOS_HIRS/.ci/compose/run-dotnet-provisioner.sh /usr/sbin/tpm_aca_provision
chmod 755 /usr/sbin/tpm_aca_provision
