#!/bin/bash

apt-get install -y sudo

wget https://packages.microsoft.com/config/ubuntu/20.04/packages-microsoft-prod.deb -O packages-microsoft-prod.deb
sudo dpkg -i packages-microsoft-prod.deb
rm packages-microsoft-prod.deb

sudo apt-get update; \
  sudo apt-get install -y apt-transport-https && \
  sudo apt-get update && \
  sudo apt-get install -y dotnet-sdk-6.0
  
cd $HIRS_CI_REPOS_HIRS/HIRS_Provisioner.NET/hirs
dotnet tool install --global dotnet-deb
dotnet tool install --global dotnet-rpm
PATH="${PATH}:~/.dotnet/tools"
dotnet deb -r linux-x64 -c Release

cp $HIRS_CI_REPOS_HIRS/HIRS_Provisioner.NET/hirs/bin/Release/net6.0/linux-x64/hirs.1.0.0.linux-x64.deb $HIRS_CI_BUILDS_DIR/

dotnet rpm -r linux-x64 -c Release

cp $HIRS_CI_REPOS_HIRS/HIRS_Provisioner.NET/hirs/bin/Release/net6.0/linux-x64/hirs.1.0.0.linux-x64.rpm $HIRS_CI_BUILDS_DIR/