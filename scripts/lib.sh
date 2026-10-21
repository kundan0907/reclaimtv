#!/usr/bin/env bash
# Shared helpers for the scripts. Sourced by them, not run on its own.
# Every write to the TV goes through tv_write or tv_push, so --dry-run prints it instead of running it.

REPO="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LISTS_DIR="${TV_CLEANUP_LISTS:-$REPO/lists}" # tests point this at fixtures
ADB="${ADB:-adb}"
DRY_RUN="${DRY_RUN:-0}"
STATE_ROOT="${TV_CLEANUP_STATE:-$HOME/.tv-cleanup}"
LAUNCHER=com.gathin.tvhome

die() { echo "error: $*" >&2; exit 1; }

need_adb() {
  command -v "$ADB" >/dev/null 2>&1 || die "adb not found. Install platform-tools (brew install android-platform-tools) or set ADB=/path/to/adb"
}

# Sets DEV (host:port) and STATE (where this TV's undo record lives) from the TV address.
set_device() {
  local addr="${1:-}"
  [[ "$addr" =~ ^[A-Za-z0-9.-]+(:[0-9]{2,5})?$ ]] || die "TV address expected, e.g. 192.0.2.10 (got '$addr')"
  case "$addr" in *:*) DEV="$addr" ;; *) DEV="$addr:5555" ;; esac
  STATE="$STATE_ROOT/${DEV//:/_}"
  need_adb
}

ensure_connected() {
  local out
  out="$("$ADB" connect "$DEV" 2>&1)" || true
  case "$out" in *connected*) ;; *) die "cannot reach $DEV ($out). Turn on ADB debugging and check the address." ;; esac
}

# Stdin goes to /dev/null: adb shell would otherwise read the rest of a loop's input.
tv_read() { "$ADB" -s "$DEV" shell "$1" </dev/null | tr -d '\r'; }

tv_write() {
  if [ "$DRY_RUN" = 1 ]; then echo "[dry-run] adb shell $1"; else "$ADB" -s "$DEV" shell "$1" </dev/null >/dev/null; fi
}

tv_push() {
  if [ "$DRY_RUN" = 1 ]; then echo "[dry-run] adb push $1 $2"; else "$ADB" -s "$DEV" push "$1" "$2" >/dev/null; fi
}

valid_pkg() { [[ "$1" =~ ^[A-Za-z_][A-Za-z0-9_]*(\.[A-Za-z0-9_]+)+$ ]]; }

# Package names from a list file. Comments, blank lines, CRLF and a UTF-8 BOM are dropped (Windows editors add them).
# Any other entry stops the run: the name ends up inside a shell command on the TV.
load_list() {
  local line
  LC_ALL=C awk 'NR==1{sub(/^\357\273\277/,"")} {sub(/\r$/,""); print}' "$1" \
    | sed -E 's/[[:space:]]*#.*$//; s/^[[:space:]]+//; s/[[:space:]]+$//' | { grep -v '^$' || true; } \
    | while IFS= read -r line; do
        valid_pkg "$line" || die "not a package name in $1: '$line'"
        echo "$line"
      done
}

# Restarts Home so it rereads the overlay. Force-stop drops the launcher's accessibility services (HomeKeyService),
# so they are put back as they were, with a pause for Android to settle in between.
restart_launcher() {
  tv_write "a=\$(settings get secure enabled_accessibility_services); am force-stop $LAUNCHER; am start -n $LAUNCHER/.MainActivity >/dev/null; case \"\$a\" in *$LAUNCHER*) sleep 1; settings put secure enabled_accessibility_services ''; settings put secure accessibility_enabled 0; sleep 1; settings put secure enabled_accessibility_services \"\$a\"; settings put secure accessibility_enabled 1;; esac"
}
