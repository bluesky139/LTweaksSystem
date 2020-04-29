#!/bin/bash

revert() {
  echo ""
  pushd $1
  pwd
  git reset --hard
  popd
}

revert build/soong
revert libcore
revert frameworks/base
revert frameworks/opt/telephony
revert packages/apps/Settings
revert packages/modules/NetworkStack
revert art
