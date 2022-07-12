# Wait for ACA to boot

echo "Waiting for ACA to spin up at address $1 on port ${HIRS_ACA_PORTAL_PORT} ..."
until [ "`curl --silent --connect-timeout 1 -I -k https://$1:${HIRS_ACA_PORTAL_PORT}/HIRS_AttestationCAPortal | grep '302 Found'`" != "" ]; do
  sleep 5;
done
echo "ACA is up!"