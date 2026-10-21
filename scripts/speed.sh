#!/usr/bin/env bash
# Shorten TV animations: window, transition and animator scales. The old values are recorded for revert.sh.
# Usage: scripts/speed.sh <tv-address> [--scale 0.5] [--dry-run]
set -euo pipefail
source "$(dirname "$0")/lib.sh"

[ $# -ge 1 ] || die "usage: speed.sh <tv-address> [--scale 0.5] [--dry-run]"
set_device "$1"
shift
SCALE=0.5
while [ $# -gt 0 ]; do
  case "$1" in
    --scale) SCALE="${2:-}"; shift ;;
    --dry-run) DRY_RUN=1 ;;
    *) die "unknown option: $1" ;;
  esac
  shift
done
[[ "$SCALE" =~ ^(0|1|0?\.[0-9]+)$ ]] || die "scale must be between 0 and 1, got '$SCALE'"

ensure_connected
[ "$DRY_RUN" = 1 ] || mkdir -p "$STATE"
for key in window_animation_scale transition_animation_scale animator_duration_scale; do
  old="$(tv_read "settings get global $key")"
  if [ "$old" = null ]; then old=1; fi
  # Record only the first value: a second run would otherwise record the already-shortened one.
  if [ "$DRY_RUN" != 1 ] && ! grep -q "^$key " "$STATE/scales.txt" 2>/dev/null; then
    echo "$key $old" >> "$STATE/scales.txt"
  fi
  tv_write "settings put global $key $SCALE"
  echo "$key: $old -> $SCALE"
done
