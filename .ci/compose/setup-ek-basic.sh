#!/bin/bash

tpm2_startup -c &
sleep 5
ek_cert=$HIRS_CI_REPOS_HIRS/.ci/setup/certs/ek_cert.der
echo "Writing Endorsement Key to Simulator"

distCmd=
if distCmd="$( which apt )" 2> /dev/null; then
   echo "Debian-based"
elif distCmd="$( which yum )" 2> /dev/null; then
   echo "Modern Red Hat-based"
else
   echo "Couldn't detect distribution command" >&2
   exit 1
fi

TPM2_TOOLS_VER_1=$("$distCmd" list installed tpm2-tools 2> /dev/null | grep --quiet -E "[ \t]+1\." && echo "1" || echo "")
TPM2_TOOLS_VER_2=$("$distCmd" list installed tpm2-tools 2> /dev/null | grep --quiet -E "[ \t]+2\." && echo "1" || echo "") 
TPM2_TOOLS_VER_3=$("$distCmd" list installed tpm2-tools 2> /dev/null | grep --quiet -E "[ \t]+3\." && echo "1" || echo "")
TPM2_TOOLS_VER_4=$("$distCmd" list installed tpm2-tools 2> /dev/null | grep --quiet -E "[ \t]+[4-9]+\." && echo "1" || echo "")
indexCmd="-x ""$TPM2_EK_NV_INDEX"

# Use tpm2_nvlist to see the size of the entry at the TPM2_EK_NV_INDEX
if [ -n "$TPM2_TOOLS_VER_1" ] || [ -n "$TPM2_TOOLS_VER_2" ]; then
	if tpm2_nvlist | grep -q 0x1c00002; then      echo "Released NVRAM for EK.";      tpm2_nvrelease -x 0x1c00002 -a 0x40000001;    fi
	size=$(cat $ek_cert | wc -c)
	tpm2_nvdefine -x 0x1c00002 -a 0x40000001 -t 0x2000A -s $size
	tpm2_nvwrite -x 0x1c00002 -a 0x40000001 -f $ek_cert
	if tpm2_nvlist | grep -q 0x1c90000; then      echo "Released NVRAM for PC.";      tpm2_nvrelease -x 0x1c90000 -a 0x40000001;    fi
elif [ -n "$TPM2_TOOLS_VER_3" ] || [ -n "$TPM2_TOOLS_VER_4" ]; then
	if tpm2_nvreadpublic | grep -q 0x1c00002; then      echo "Released NVRAM for EK.";      tpm2_nvundefine 0x1c00002 -C o;    fi
	size=$(cat $ek_cert | wc -c)
	tpm2_nvdefine -C o -a 0x2000A -s $size 0x1c00002 
	tpm2_nvwrite -C o -i $ek_cert 0x1c00002 
	if tpm2_nvreadpublic | grep -q 0x1c90000; then      echo "Released NVRAM for PC.";      tpm2_nvundefine 0x1c90000 -C o;    fi
else
	echo "Please install tpm2-tools"
	exit 1
fi


