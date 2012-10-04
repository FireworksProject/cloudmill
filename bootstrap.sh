#!/usr/bin/env sh

vboxwebsrv -t0
VBoxManage setproperty websrvauthlibrary null
