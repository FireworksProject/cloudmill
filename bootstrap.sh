#!/usr/bin/env sh

cd `dirname $0`

vboxwebsrv -t0
VBoxManage setproperty websrvauthlibrary null

if ! [ -e ~/.pallet/config.clj ]; then
    cp resources/pallet-config.clj ~/.pallet/config.clj
fi
