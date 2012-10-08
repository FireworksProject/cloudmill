#!/usr/bin/env sh

cd `dirname $0`

PALLET_CONF_FILE="$HOME/.pallet/config.clj"
DEFAULT_PALLET_CONF="./resources/pallet-config.clj"

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

ensure_available "lein"       "Ensure you have leiningen installed: https://github.com/technomancy/leiningen"
ensure_available "vboxwebsrv" "Ensure you have installed VirtualBox"
ensure_available "VBoxManage" "Ensure you have installed VirtualBox"

if ! [ -f "$PALLET_CONF_FILE" ]; then
    ensure_dir `dirname $PALLET_CONF_FILE`
    cp $DEFAULT_PALLET_CONF $PALLET_CONF_FILE  || warn "Couldn't copy $DEFAULT_PALLET_CONF to $PALLET_CONF_FILE"
fi

# FIXME:
# Currently pallet uses ssh-agent to discover which key to use for
# shelling into the nodes it creates. You will not be able to shell in
# to the new nodes if you don't have a key registered with ssh-agent,
# and therefore pallet will not be able to set up the nodes you want.
# This code ensures there is a key registered. I'm not sure if this is
# the best way to do this. Please feel free to fix this.
if ! [ -f "$HOME/.ssh/id_rsa" ]; then
    ssh-keygen -t rsa
fi
# The -K switch is not available on Ubuntu 10.04
# ssh-add -K "$HOME/.ssh/id_rsa" || fail "Could not add ssh key to ssh-agent"
ssh-add "$HOME/.ssh/id_rsa" || fail "Could not add ssh key to ssh-agent"


VBoxManage setproperty websrvauthlibrary null || fail "Failure invoking: VBoxManage setproperty websrvauthlibrary null"
vboxwebsrv -t0 || fail "Failure invoking: vboxwebsrv -t0"
