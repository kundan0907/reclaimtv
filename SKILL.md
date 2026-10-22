---
name: reclaimtv
description: Clean up an Android TV or Google TV (any brand): debloat with reversible steps, shorten animations, set up IPTV in OwnTV from iptv-org playlists, install SmartTube, apply a launcher theme (Dark, Batman, Tesla, Aurora or Compact, or one of 16 bundled themes) and launcher options (glass icons, dock magnification, weather, calendar, widgets, console tile), and undo any of it. Use when the user asks to clean up, debloat, speed up or personalise their TV, set up IPTV or SmartTube on it, pick or change a TV theme, or revert a previous cleanup.
---

# TV cleanup

Work from the folder this file is in. Scripts are `scripts/*.sh` and `scripts/*.py`; lists, themes and the app catalog are beside them.

## Rules

- Ask before every change. Show the plan, then run it.
- Dry-run first: `--dry-run` prints every write and runs none.
- Only reversible changes: packages are disabled for user 0, never uninstalled. Each run is recorded so `revert.sh` can undo it.
- Never edit `lists/keep.txt` without the user's say-so. It holds the Play Store, Downloader and the launcher.
- Never overwrite a dock the user arranged on the TV. Pass `--dock` only with the order the user confirms.
- The TV address is an argument. Never hard-code one. Only the IPTV list comes from the internet; channels must be ones the user may watch.
- Read `docs/safety.md` before the first run.

## Steps

1. **Questionnaire.** Ask:
   - Theme: `dark`, `batman`, `tesla`, `aurora` or `compact`, or a bundled theme (`scripts/launcher_options.py --list-themes`; `themes/README.md`). Light mode: yes or no (default no).
   - Launcher options, each with its default. Decide from the answers; do not make the user pick flags.
     - Weather (default no), calendar and days-left cards (no), widget row with continue-watching (no).
     - Calendar mode weekly or monthly (weekly), widget size small, medium or large (medium).
     - Dock magnification and glass dock (no), glass or original icons (the launcher's own), icon-only top bar (no).
     - Theme rotation: off, 15 minutes, hourly or daily (off), and screensaver widgets.
     - Console: "Do you have a PlayStation, Xbox or Switch on HDMI?" Only if yes, add the tile (`--console "PlayStation"`).
   - Privacy, asked before enabling, one plain sentence each: weather sends the TV's approximate location (from its public IP) to ipwho.is and Open-Meteo; the continue-watching picture needs notification access for the launcher and, for a YouTube app, fetches YouTube's public thumbnail and title.
   - Dock apps: pick from `catalog/apps.json`. Ask whether they arranged the dock on the TV; if so, keep it.
   - Debloat level: `light` (Google extras) or `full` (adds TV-maker promotions).
   - Animation speed: `0.5` is a good default, `1` leaves animations alone.
   - IPTV: yes or no. If yes, which country code (`us`, `in`, ...) and which categories (`news`, `sports`, ...).
   - SmartTube: yes or no.
2. **Enable ADB.** Follow `docs/setup.md` on the TV. Ask for the TV's IP address.
3. **Connect.** `scripts/connect.sh <tv-ip>` prints the model and Android version. If `adb_secure` is `0`, remind the user to turn ADB off afterwards.
4. **Debloat.** `scripts/debloat.sh <tv-ip> light --dry-run`, show the plan, then run it without `--dry-run`. Skipped lines say why.
5. **Speed.** `scripts/speed.sh <tv-ip> --scale 0.5` (or the scale chosen).
6. **Theme and options.** The launcher APK must be installed first (`docs/launcher.md`, `launcher/build-and-install.sh`). For a preset: `scripts/apply-theme.sh <tv-ip> themes/<name>`. For launcher options, write an overlay and push it:
   - `python3 scripts/launcher_options.py --theme <id> [--light] [--weather] [--year-widget] [--widget-row] [--magnify] [--glass-icons] [--console "PlayStation"] --out mytv`
   - `scripts/apply-theme.sh <tv-ip> mytv --dry-run`, then without `--dry-run`.
   - Pass `--dock a,b,c` only if the user chose a dock. This restarts Home.
   - The script prints notes for choices made on the TV (calendar mode, widget size, theme rotation, a bundled theme's wallpaper, notification access). Walk the user through each one.
7. **IPTV (optional).**
   - `python3 scripts/iptv_m3u.py fetch countries <cc> iptv.m3u`, or `fetch categories <name> iptv.m3u`.
   - `python3 scripts/iptv_m3u.py verify iptv.m3u iptv-ok.m3u --min-height 480`.
   - Import `iptv-ok.m3u` into OwnTV using `docs/owntv.md`.
8. **SmartTube (optional).** Install from the official release, as in `docs/smarttube.md`.
9. **Check.** `adb -s <tv-ip>:5555 exec-out screencap -p > home.png` shows the home screen. Ask the user to press Home once and to try one IPTV channel.
10. **Finish.** Ask the user to turn ADB debugging off. Report what was disabled, using the record in `~/.tv-cleanup/<tv>/`.

## Undo

- `scripts/revert.sh <tv-ip>` re-enables every package and restores the animation scales from the record.
- `scripts/apply-theme.sh <tv-ip> --reset` removes the theme overlay and launcher options. Choices made on the TV are undone in the same menu. Notification access: `cmd notification disallow_listener com.gathin.tvhome/com.gathin.tvhome.MediaWatcher`.
- Troubleshooting: `docs/troubleshooting.md`.

## Install (optional)

Installing as a skill is optional. Any AI assistant can follow `START_HERE.md` instead.

- **Claude Code:** `git clone https://github.com/kundan0907/reclaimtv ~/.claude/skills/reclaimtv`
- **Codex:** `git clone https://github.com/kundan0907/reclaimtv ~/.agents/skills/reclaimtv` (Codex reads `~/.agents/skills`; use `~/.codex/skills` if your setup does).
- Requires `adb` (Android platform-tools) and `python3`. The tests need nothing else: `python3 -m unittest discover -s tests`.
