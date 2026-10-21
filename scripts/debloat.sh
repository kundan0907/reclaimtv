#!/usr/bin/env bash
# Disable the packages in a debloat list that are installed on the TV. Nothing is uninstalled; revert.sh undoes it.
# Usage: scripts/debloat.sh <tv-address> <light|full> [--dry-run] [--allow-home] [--allow-setupwraith]
#   light  lists/debloat-google.txt
#   full   lists/debloat-google.txt and lists/debloat-firmware.txt
set -euo pipefail
source "$(dirname "$0")/lib.sh"

[ $# -ge 2 ] || die "usage: debloat.sh <tv-address> <light|full> [--dry-run]"
set_device "$1"
LEVEL="$2"
shift 2
ALLOW_HOME=0
ALLOW_SETUPWRAITH=0
for arg in "$@"; do
  case "$arg" in
    --dry-run) DRY_RUN=1 ;;
    --allow-home) ALLOW_HOME=1 ;;
    --allow-setupwraith) ALLOW_SETUPWRAITH=1 ;;
    *) die "unknown option: $arg" ;;
  esac
done
case "$LEVEL" in
  light) LISTS=(debloat-google.txt) ;;
  full) LISTS=(debloat-google.txt debloat-firmware.txt) ;;
  *) die "level must be light or full" ;;
esac

ensure_connected
LISTED=""
for f in "${LISTS[@]}"; do LISTED+="$(load_list "$LISTS_DIR/$f")"$'\n'; done # a bad entry exits here
WANT="$(sort -u <<< "$LISTED")"
KEEP="$(load_list "$REPO/lists/keep.txt")" # always the repo's own keep list, never a test override
INSTALLED="$(tv_read 'pm list packages' | sed 's/^package://')"
DISABLED="$(tv_read 'pm list packages -d --user 0' | sed 's/^package://')"
HOME_OWNER="$(tv_read 'cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME' | tail -n 1 | cut -d/ -f1)"

[ "$DRY_RUN" = 1 ] || mkdir -p "$STATE"
while IFS= read -r pkg; do
  [ -n "$pkg" ] || continue
  if grep -qxF "$pkg" <<< "$KEEP"; then echo "keep     $pkg"; continue; fi
  if ! grep -qxF "$pkg" <<< "$INSTALLED"; then echo "absent   $pkg"; continue; fi
  if grep -qxF "$pkg" <<< "$DISABLED"; then echo "off      $pkg (already disabled)"; continue; fi
  # Disabling the setup wizard can delete the Wi-Fi it saved, which takes the TV off the network. docs/safety.md
  if [ "$pkg" = com.google.android.tungsten.setupwraith ] && [ "$ALLOW_SETUPWRAITH" = 0 ]; then
    echo "skip     $pkg (owns saved Wi-Fi; see docs/safety.md)"; continue
  fi
  # Disabling the app that answers HOME leaves nothing to press Home with. docs/safety.md
  if [ "$pkg" = "$HOME_OWNER" ] && [ "$ALLOW_HOME" = 0 ]; then
    echo "skip     $pkg (answers HOME; see docs/safety.md)"; continue
  fi
  tv_write "pm disable-user --user 0 $pkg"
  [ "$DRY_RUN" = 1 ] || echo "$pkg" >> "$STATE/disabled.txt"
  echo "disable  $pkg"
done <<< "$WANT"

if [ "$DRY_RUN" = 1 ]; then echo "dry run: nothing changed"; else echo "Undo with: scripts/revert.sh $DEV"; fi
