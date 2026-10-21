#!/usr/bin/env bash
# Re-enable every package debloat.sh disabled on this TV, and put back the animation scales speed.sh changed.
# Usage: scripts/revert.sh <tv-address> [--dry-run]
set -euo pipefail
source "$(dirname "$0")/lib.sh"

[ $# -ge 1 ] || die "usage: revert.sh <tv-address> [--dry-run]"
set_device "$1"
shift
for arg in "$@"; do
  case "$arg" in
    --dry-run) DRY_RUN=1 ;;
    *) die "unknown option: $arg" ;;
  esac
done
[ -f "$STATE/disabled.txt" ] || [ -f "$STATE/scales.txt" ] || die "nothing recorded for $DEV under $STATE_ROOT"
ensure_connected

if [ -f "$STATE/disabled.txt" ]; then
  while IFS= read -r pkg; do
    valid_pkg "$pkg" || die "bad entry in $STATE/disabled.txt: $pkg"
    tv_write "pm enable --user 0 $pkg"
    echo "enable   $pkg"
  done < <(sort -u "$STATE/disabled.txt")
fi
if [ -f "$STATE/scales.txt" ]; then
  while read -r key value; do
    tv_write "settings put global $key $value"
    echo "restore  $key=$value"
  done < "$STATE/scales.txt"
fi

if [ "$DRY_RUN" = 1 ]; then
  echo "dry run: nothing changed"
else
  rm -f "$STATE/disabled.txt" "$STATE/scales.txt"
  echo "Reverted $DEV. Nothing is uninstalled; a package can still be removed by hand from Settings."
fi
