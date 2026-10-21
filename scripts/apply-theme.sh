#!/usr/bin/env bash
# Push a theme to the launcher on the TV and restart Home. A theme is a config.json overlay; see themes/README.md.
# Usage: scripts/apply-theme.sh <tv-address> themes/<name> [--dock pkg1,pkg2] [--dry-run]
#        scripts/apply-theme.sh <tv-address> --reset [--dry-run]
# --dock replaces the theme's dock. Ask the user first: a dock they arranged on the TV is theirs to keep.
set -euo pipefail
source "$(dirname "$0")/lib.sh"

[ $# -ge 2 ] || die "usage: apply-theme.sh <tv-address> <themes/name | --reset> [--dock a,b] [--dry-run]"
set_device "$1"
THEME="$2"
shift 2
DOCK=""
while [ $# -gt 0 ]; do
  case "$1" in
    --dock) DOCK="${2:-}"; shift ;;
    --dry-run) DRY_RUN=1 ;;
    *) die "unknown option: $1" ;;
  esac
  shift
done

DIR="/sdcard/Android/data/$LAUNCHER/files"
URI="content://$LAUNCHER.theme"
STAGE=/data/local/tmp/tvcleanup-config.json
ensure_connected

if [ "$THEME" = --reset ]; then
  tv_write "rm -f $DIR/config.json 2>/dev/null; content delete --uri $URI/config.json 2>/dev/null"
else
  [ -f "$THEME/config.json" ] || die "no config.json in $THEME"
  OVERLAY="$(mktemp)"
  trap 'rm -f "$OVERLAY"' EXIT
  python3 "$REPO/scripts/theme_merge.py" "$THEME/config.json" ${DOCK:+--dock "$DOCK"} > "$OVERLAY"
  tv_push "$OVERLAY" "$STAGE"
  # Some firmware blocks the shell from Android/data; the launcher's theme provider is the fallback.
  tv_write "mkdir -p $DIR 2>/dev/null; cp $STAGE $DIR/config.json 2>/dev/null || content write --uri $URI/config.json < $STAGE; rm -f $STAGE"
fi
restart_launcher
if [ "$DRY_RUN" = 1 ]; then echo "dry run: nothing changed"; else echo "Applied $THEME. Undo with: scripts/apply-theme.sh $DEV --reset"; fi
