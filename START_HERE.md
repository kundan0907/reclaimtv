# START HERE: set up an Android TV with tv-cleanup

You are an AI assistant. The user wants their Android TV or Google TV cleaned up: unused apps disabled (reversibly), animations shortened, IPTV set up in OwnTV, SmartTube installed, and a launcher theme applied. This file is the whole procedure. Follow it in order, and ask before every change.

Repo: https://github.com/kundan0907/tv-cleanup

## Rules

1. Ask before each change. Show the plan, then wait for a yes for each step.
2. Dry-run first. Show the dry-run output before any write.
3. Never uninstall anything. Only disable, which is reversible.
4. Never edit `lists/keep.txt`. It holds the Play Store and Downloader.
5. Never overwrite a dock the user arranged on the TV. Ask first.
6. Never guess the TV's IP address. Ask for it.
7. IPTV only from the iptv-org index, and only channels the user may watch. No piracy streaming apps. No paid IPTV resellers.
8. At the end, ask the user to turn ADB debugging off.

## Step 0: Where are you running?

Ask: "Can you run commands on a computer on the same Wi-Fi as the TV, such as Claude Code or the Codex CLI?"

- **Yes: local mode.** Follow Step 2.
- **No: cloud chat only.** A cloud chat cannot reach a TV on the home network, so the ADB steps cannot run from here. Plan and guide instead. Give the user one command at a time, with the same questions and rules, and ask them to paste each output back.

## Step 1: Ask the questions

Ask all of these in one message:

1. TV brand and model (from the box, or Settings, then About). Wi-Fi or Ethernet?
2. Their computer: macOS, Windows or Linux.
3. Theme: `dark`, `batman`, `tesla`, `aurora` or `compact`, or one of the bundled themes (`python3 scripts/launcher_options.py --list-themes`). Describe each from `themes/README.md`. Light mode: yes or no (default no).
4. Dock apps to keep. Offer the apps in `catalog/apps.json` that they have installed. The default dock is YouTube, SmartTube, OwnTV, Tubi and Pluto TV.
5. IPTV: on or off? If on, a country code (for example `us`, `in`) or a category (for example `news`, `sports`).
6. Debloat level: `light` (Google extras) or `full` (also TV-maker promotions).
7. Animation speed: `0.5` (faster) or `1` (unchanged).
8. Launcher options. Offer each with its default, then decide the flags yourself from the answers:
   - Weather: yes or no (default no).
   - Calendar and days-left-in-year cards: yes or no (no). Calendar mode: weekly or monthly (weekly).
   - Widget row, with a continue-watching tile: yes or no (no). Widget size: small, medium or large (medium).
   - Dock magnification and a glass dock: yes or no (no). Glass icons or the apps' original icons (the launcher's own).
   - Icon-only top bar: yes or no (no).
   - Theme rotation: off, every 15 minutes, hourly or daily (off). Screensaver widgets: yes or no.
   - "Do you have a PlayStation, Xbox or Switch on HDMI?" Only if yes, add its tile.

Before enabling these two, tell the user the privacy impact in one sentence each and wait for a yes:

- Weather sends the TV's public IP to ipwho.is (once a day) for an approximate location, and the location to Open-Meteo for the forecast.
- The continue-watching picture needs notification access for the launcher. For a YouTube app it fetches YouTube's public thumbnail and title for the video that was playing.

## Step 2: Local mode

a. Get the repo: `git clone https://github.com/kundan0907/tv-cleanup`, then work inside that folder.

b. Ask the user to turn on ADB debugging on the TV (`docs/setup.md`), and to give you the TV's IP address.

c. Connect and show the output:

    scripts/connect.sh <TV_IP>

   If `adb_secure` is `0`, warn that ADB must be turned off afterwards.

d. Debloat. Show the plan, then wait for a yes:

    scripts/debloat.sh <TV_IP> <light|full> --dry-run

   After the yes, run it without `--dry-run`. Lines marked `skip` or `keep` are not changed; say why.

e. Speed, if chosen. Show the dry-run, then wait for a yes:

    scripts/speed.sh <TV_IP> --scale 0.5 --dry-run

f. Theme and launcher options. The launcher APK must be built and installed first (`docs/launcher.md`); ask before installing. For a preset, show the dry-run, then wait for a yes:

    scripts/apply-theme.sh <TV_IP> themes/<name> --dry-run

   For launcher options, write an overlay from the answers (the flags are in `docs/launcher.md`), then dry-run it:

    python3 scripts/launcher_options.py --theme <id> --weather --year-widget --widget-row --magnify --glass-icons --out mytv
    scripts/apply-theme.sh <TV_IP> mytv --dry-run

   Add `--console "PlayStation"` only if the user has one on HDMI. Add `--light` only if they chose light mode.

   If the user arranged the dock on the TV, ask before adding `--dock a,b,c`. Without the yes, do not pass it.

   The script prints notes for choices made on the TV: calendar mode, widget size, theme rotation, a bundled theme's wallpaper (gear > Theme), and notification access for the continue-watching picture. Walk the user through each one, and run the `allow_listener` command only after their yes.

g. IPTV, if on:

    python3 scripts/iptv_m3u.py fetch countries <cc> iptv.m3u
    python3 scripts/iptv_m3u.py verify iptv.m3u iptv-ok.m3u --min-height 480

   Use `fetch categories <name>` instead for a category. Then import `iptv-ok.m3u` into OwnTV as described in `docs/owntv.md`. The verify step checks that the playlist answers; it does not guarantee that each channel plays.

h. SmartTube, if chosen. The user installs it from the official release, as described in `docs/smarttube.md`. Do not download or install an APK yourself.

i. Check. Ask the user to press Home once and to try one IPTV channel.

j. Finish. Ask the user to turn ADB debugging off. Summarise what changed. Tell them how to undo:

    scripts/revert.sh <TV_IP>
    scripts/apply-theme.sh <TV_IP> --reset

## If something goes wrong

Read `docs/troubleshooting.md` and `docs/safety.md`. Do not retry a failed write without asking.

## Reference

- `docs/setup.md`: turning on ADB
- `docs/safety.md`: guards and limits
- `themes/README.md`: theme keys and presets
- `docs/owntv.md`, `docs/smarttube.md`, `docs/free-movies.md`
- `docs/launcher.md`: the launcher, its options, privacy notes and how to build it
