#!/bin/bash

distCmd=
if distCmd="$( which apt-get )" 2> /dev/null; then
   apt-get install -y sudo tpm2-tools build-essential unzip vim
elif distCmd="$( which yum )" 2> /dev/null; then
   yum install -y sudo tpm2-tools unzip vim
else
   echo "Couldn't detect distribution command" >&2
   exit 1
fi


mkdir -p /tmp
cd /tmp
curl -L -o sim.tar.gz https://downloads.sourceforge.net/project/ibmswtpm2/ibmtpm1661.tar.gz
mkdir ibm-tpm
tar -xvzf sim.tar.gz -C ./ibm-tpm
cd ./ibm-tpm
cd src
sudo make
sudo ./tpm_server & 
sleep 5

echo "Placing test artifacts"
sudo mkdir -p /home/runner/work/efi
sudo mkdir -p /home/runner/work/efi/EFI/tcg/cert/platform
sudo mkdir -p /home/runner/work/efi/EFI/tcg/manifest/swidtag
sudo mkdir -p /home/runner/work/efi/EFI/tcg/manifest/rim
sudo mkdir -p /home/runner/work/hw
sudo mkdir -p /home/runner/work/tpm
sudo mkdir -p /home/runner/work/sys/class
sudo unzip -q $HIRS_CI_REPOS_HIRS/.ci/system-tests/profiles/laptop/laptop_dmi.zip -d /home/runner/work/sys/class/
sudo chmod -R 444 /home/runner/work/sys