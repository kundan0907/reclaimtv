#!/usr/bin/env bash
# Connect to a TV over ADB and print the facts the other scripts depend on.
# Usage: scripts/connect.sh <tv-address>
set -euo pipefail
source "$(dirname "$0")/lib.sh"

[ $# -ge 1 ] || die "usage: connect.sh <tv-address>"
set_device "$1"
ensure_connected
echo "model:      $(tv_read 'getprop ro.product.model')"
echo "brand:      $(tv_read 'getprop ro.product.brand')"
echo "android:    $(tv_read 'getprop ro.build.version.release') (sdk $(tv_read 'getprop ro.build.version.sdk'))"
echo "ram:        $(tv_read 'grep MemTotal /proc/meminfo' | tr -s ' ')"
SECURE="$(tv_read 'getprop ro.adb.secure')"
echo "adb_secure: $SECURE"
[ "$SECURE" = 1 ] || echo "warning: this firmware lets any device on your network open a shell without a prompt. Turn ADB off when you are done." >&2
