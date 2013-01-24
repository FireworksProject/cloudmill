#!/usr/bin/env sh

cd `dirname $0`

fail () {
    echo "$@" >&2
    exit 1
}

warn () {
    echo "$@" >&2
}

ensure_dir () {
    if ! [ -d "$1" ]; then
        mkdir -p -- "$1" || fail "couldn't create $1"
    fi
}

ensure_available () {
    if [ -z `which $1` ]; then
        fail "ERROR:\tCould not find $1 on your path.\n\t$2"
        exit 1
    fi
}

ensure_available "vboxwebsrv" "Ensure you have installed VirtualBox"
ensure_available "VBoxManage" "Ensure you have installed VirtualBox"

VBoxManage setproperty websrvauthlibrary null || fail "Failure invoking: VBoxManage setproperty websrvauthlibrary null"
vboxwebsrv -t0 || fail "Failure invoking: vboxwebsrv -t0"
