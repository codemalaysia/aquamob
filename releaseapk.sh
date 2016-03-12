#!/usr/bin/env bash
echo -n "Password: "
read -s password
echo

if [ "$password" != "rele@se" ]; then
  echo "Wrong password. Exiting";
  exit;
fi

export RELEASE_TYPE=release
./release.sh assembleRelease
