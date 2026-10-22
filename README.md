# reclaimtv

**reclaimtv restores ownership of a television's hardware by removing manufacturer promotions, telemetry and background bloat without uninstalling anything. It replaces the stock home screen with a minimalist glass interface that shows only the apps you choose.**

Target hardware: Android TV and Google TV. Works as a skill for Claude Code and Codex, or with any assistant that can read this repository.

---

## Overview

![A glass Android TV launcher in eight themes: Northern Lights, Violet Aurora, Synthwave, Sunset, Carbon, Rosso Corsa, Iron Man arc reactor and Daylight](docs/screenshots/hero-themes.jpg)

<p align="center"><img src="docs/screenshots/theme-cycle.gif" width="640" alt="The launcher cycling through its 16 themes"></p>

### Before and After

| Before: stock Google TV home | After: reclaimtv launcher |
|---|---|
| ![Stock Google TV home: a featured title, a top picks row and every app in one long row](docs/screenshots/stock-home-before.png) | ![reclaimtv home: a five-app dock, the clock, Inputs and Settings, on a dark city background](docs/screenshots/home-gotham-after.png) |

| Aspect | Stock firmware | With reclaimtv |
|---|---|---|
| Home screen | A featured title, "Top picks" rows and every app in one long row | The apps you chose, in the order you set, in a dock |
| Background packages | Google and TV-maker promotion apps run | Those packages are disabled, not uninstalled (`lists/`) |
| Protected packages | Not applicable | The Play Store and Downloader are never touched (`lists/keep.txt`) |
| Animations | Default scales | Shorter scales (`scripts/speed.sh`) |
| Playback on Home | A video or song keeps playing behind the home screen | Pressing Home pauses it |
| Reversibility | Not applicable | Every change has an undo: `scripts/revert.sh` |
| Free viewing | Subscriptions and ad-supported apps | Optional SmartTube for YouTube without ads, and OwnTV with public iptv-org channels |

No speed or memory gain is claimed; the repository has not measured either.

---

## Technical Features

### System Optimization

- `scripts/debloat.sh` disables the packages in `lists/` with `pm disable-user --user 0` and never uninstalls them.
- `lists/keep.txt` is checked first, so the Play Store, Downloader, the launchers and the setup wizard are skipped.
- `scripts/speed.sh` shortens the window, transition and animator scales and records the old values.
- `--dry-run` prints the plan for every script before anything is written to the TV.
- `scripts/revert.sh` re-enables every recorded package and restores the recorded animation scales.

### Interface Architecture

- `launcher/` builds the home screen as an ordinary Android app (`com.gathin.tvhome`) that declares the HOME category.
- The dock magnifies the focused app, and every tile under it shares the theme-coloured glow.
- Pressing Up tucks the dock into a thin lip and leaves the calendar and year card on screen.
- The calendar shows the week or the month, and the year card counts the days left.
- A widget row can hold a continue-watching tile that shows the thumbnail and title of the last video.
- The clock screensaver moves the clock, date, weather, calendar and year card together.
- A PlayStation, Xbox or Switch tile appears when one is connected on HDMI.
- Pressing Home pauses any media that is playing, and does nothing when nothing is playing.
- The launcher switches Home only after it has started, then checks who owns Home and restores the stock launcher on failure (`launcher/build-and-install.sh`).

### Local Configuration

- `themes/` holds colour presets, and `scripts/apply-theme.sh` pushes one `config.json` overlay that the launcher reads over its built-in settings.
- `scripts/launcher_options.py` writes the same overlay from flags, and `apply-theme.sh <tv> --reset` removes it.
- The gear menu on the TV sets theme, theme rotation, widget size, glass or original icons, and wallpaper with the remote.
- Every launcher option is off until you choose it (`docs/launcher.md`).
- Weather and the continue-watching picture use the internet, so they stay off until you agree.
- The assistant offers two optional installs for free viewing without a subscription: SmartTube, a YouTube client with no ads, and OwnTV with a playlist from the public iptv-org index. Neither installs without your approval (`docs/smarttube.md`, `docs/owntv.md`).
- SmartTube installs from its official release, and OwnTV loads channels that `iptv/` has checked and filtered.
- Free movie sources are listed in `docs/free-movies.md`.

---

## Installation

Both options need ADB debugging turned on at the TV (`docs/setup.md`). Turn it off when you finish.

### Option A: Automated, with a local terminal assistant

Use Claude Code or the Codex CLI on a computer on the same Wi-Fi as the TV. A cloud chat can plan and guide, but it cannot reach a TV on your home network.

1. Paste this prompt into the assistant:

```text
Set up my Android TV with https://github.com/kundan0907/reclaimtv — start with START_HERE.md and ask before each change.
```

2. Answer the questions about your TV, network, theme, apps, IPTV and debloat level.
3. Approve each step after its dry run is shown. The assistant asks you to turn ADB off at the end.

To install the repository as a skill (`SKILL.md`):

```bash
git clone https://github.com/kundan0907/reclaimtv ~/.claude/skills/reclaimtv
git clone https://github.com/kundan0907/reclaimtv ~/.agents/skills/reclaimtv
```

The first line is for Claude Code and the second for Codex (use `~/.codex/skills` if your setup does).

### Option B: Manual, with ADB and the scripts

Replace `<tv-ip>` with the TV's address. Each command is shown before it changes anything when you add `--dry-run`.

1. Connect, then accept the prompt on the TV (`docs/setup.md`):

```bash
adb connect <tv-ip>:5555
scripts/connect.sh <tv-ip>
```

2. Preview the debloat plan, then apply it (`light` uses `lists/debloat-google.txt`; `full` adds `lists/debloat-firmware.txt`):

```bash
scripts/debloat.sh <tv-ip> light --dry-run
scripts/debloat.sh <tv-ip> light
```

   The script runs this for each listed package that is installed:

```bash
adb -s <tv-ip>:5555 shell pm disable-user --user 0 <package>
```

3. Shorten the animations:

```bash
scripts/speed.sh <tv-ip> --scale 0.5
```

4. Build the launcher and make it Home (JDK 17 and Android SDK 34, see `docs/launcher.md`):

```bash
cd launcher
./build-and-install.sh <tv-ip>
```

5. Apply a theme:

```bash
scripts/apply-theme.sh <tv-ip> themes/dark
```

#### Prebuilt launcher APK

To update a TV that already runs this launcher, download `tv-home.apk` from the [latest release](https://github.com/kundan0907/reclaimtv/releases) and install it:

```bash
adb install -r --user 0 tv-home.apk
```

This installs the app only. It does not make the launcher Home and it does not debloat. The APK is signed with the project's debug key, so it updates over a build from this repository without clearing settings. A copy signed with another key must be uninstalled first, which resets the launcher.

---

## System Integrity and Rollback

### Why the scripts do not brick hardware

- The scripts use `adb shell` as the unprivileged shell user, with no root, no firmware flashing and no bootloader changes (`docs/safety.md`).
- `pm disable-user --user 0` changes one flag in the package manager for user 0. The package stays on the device and no partition is written.
- Each script changes only a package's enabled state, the global animation scales, one staged `config.json`, or an installed app.
- Because every change is a reversible setting or an ordinary app, the device always boots the same firmware it shipped with.

### Known failure modes and their guards

- Disabling the setup wizard (`com.google.android.tungsten.setupwraith`) can delete the Wi-Fi it saved, which takes the TV off the network. It is on `lists/keep.txt` and skipped unless you pass `--allow-setupwraith`.
- Disabling the app that answers HOME can leave Home with nothing to open. It is skipped unless you pass `--allow-home`.
- List entries must be valid package names and the TV address must be an IP or host name, so nothing unvalidated reaches a shell.
- On some firmware (`ro.adb.secure=0`), any device on your network can open a shell on port 5555. Turn ADB on only while cleaning up.

### Reverting

```bash
scripts/revert.sh <tv-ip> --dry-run
scripts/revert.sh <tv-ip>
scripts/apply-theme.sh <tv-ip> --reset
```

- `revert.sh` runs `pm enable --user 0` for every package recorded as disabled and restores the recorded animation scales.
- `apply-theme.sh <tv-ip> --reset` removes the `config.json` overlay.
- Packages that were already disabled before the first run are not recorded, so they stay disabled.
- This restores the state recorded before the first run. It is not a factory reset, and it does not uninstall the launcher or any app you added.
- To return Home to the stock launcher, re-enable it and set it as the Home activity (the package varies by TV, for example `com.google.android.apps.tv.launcherx`):

```bash
adb -s <tv-ip>:5555 shell pm enable --user 0 <stock-launcher-package>
adb -s <tv-ip>:5555 shell cmd package set-home-activity --user 0 $(adb -s <tv-ip>:5555 shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME <stock-launcher-package> | tail -n 1)
```

---

## Interface Presets

Sixteen themes ship in the APK. Each sets an accent colour that tints the dock light, the widgets and the focus glow. Choose one in gear > Theme, or let them rotate.

| Theme | Accent | Preview |
|---|---|---|
| Northern Lights | `#52E08A` | ![Northern Lights](docs/screenshots/themes/northern.jpg) |
| Violet Aurora | `#C65BFF` | ![Violet Aurora](docs/screenshots/themes/violet.jpg) |
| Synthwave | `#FF4FD8` | ![Synthwave](docs/screenshots/themes/synthwave.jpg) |
| Sunset | `#FF8A5B` | ![Sunset](docs/screenshots/themes/sunset.jpg) |
| Aurora | `#3FD0C0` | ![Aurora](docs/screenshots/themes/aurora.jpg) |
| Carbon | `#4DA3FF` | ![Carbon](docs/screenshots/themes/carbon.jpg) |
| Gotham | `#A9B4C8` | ![Gotham](docs/screenshots/themes/gotham.jpg) |
| Iron Man · Arc reactor | `#35D6F5` | ![Iron Man · Arc reactor](docs/screenshots/themes/ironman-arc.jpg) |
| Black Panther · Vibranium | `#8A5CF5` | ![Black Panther · Vibranium](docs/screenshots/themes/panther-vibranium.jpg) |
| RCB | `#E0252B` | ![RCB](docs/screenshots/themes/rcb.jpg) |
| Rosso Corsa | `#E03A3A` | ![Rosso Corsa](docs/screenshots/themes/rosso.jpg) |
| Mountains | `#8D99AE` | ![Mountains](docs/screenshots/themes/mountains.jpg) |
| Daylight | `#5B8DEF` | ![Daylight](docs/screenshots/themes/daylight.jpg) |
| Aurora · Light | `#1FA896` | ![Aurora · Light](docs/screenshots/themes/aurora-light.jpg) |
| Rosso Corsa · Light | `#C9302C` | ![Rosso Corsa · Light](docs/screenshots/themes/rosso-light.jpg) |
| Mountains · Light | `#6B7A90` | ![Mountains · Light](docs/screenshots/themes/mountains-light.jpg) |

Five colour-only presets (`dark`, `batman`, `tesla`, `aurora`, `compact`) also ship in `themes/` and need no wallpaper. Details: `themes/README.md`. The Gotham picture in "Before and After" is from the author's TV.

---

## Interface Gallery

#### Glass dock

The dock magnifies the app under focus, and a soft light in the theme's colour flows along it. Every tile under the dock lights the same soft theme-coloured glow when you focus it, so the whole home behaves as one piece.

![The home screen: a glass dock with five apps, a row of apps, and a widget row with calendar, year, weather and a continue-watching tile](docs/screenshots/features/dock.jpg)

#### Widgets that stay out of the way

Press Up and the dock slides down to a thin lip. The calendar and the year card stay on screen, top right, stacked under the three buttons.

| Dock tucked away | Widget row |
|---|---|
| ![Dock hidden: clock, weather, calendar and year card on the wallpaper](docs/screenshots/features/dock-hidden.jpg) | ![Widget row with calendar, year, weather and a continue-watching tile that shows the video you left](docs/screenshots/features/widget-row.jpg) |

The continue-watching tile shows the real thumbnail and title of the last video (Big Buck Bunny by the Blender Foundation, CC BY 3.0, in these pictures).

#### Calendar and year

Open the calendar with OK. It shows the week or the whole month, and remembers your choice. The year card counts the days left.

| Weekly | Monthly | Days left in the year |
|---|---|---|
| ![Weekly calendar, October 18 to 24, today in green](docs/screenshots/features/calendar-weekly.jpg) | ![Monthly calendar for October 2026](docs/screenshots/features/calendar-monthly.jpg) | ![Year card: 71 days left, a dot for every day of 2026](docs/screenshots/features/year.jpg) |

#### Settings on the TV

Everything is chosen on the TV with the remote: theme, theme rotation, widget size, glass or original icons, wallpaper.

| Menu | Theme picker | Launcher settings |
|---|---|---|
| ![The gear menu](docs/screenshots/features/menu.jpg) | ![Theme picker](docs/screenshots/features/theme-picker.jpg) | ![Launcher settings](docs/screenshots/features/launcher-settings.jpg) |

#### Screensaver

The clock, date, weather, calendar and year card drift together at the top left, the cards stacked one under the other.

![Screensaver: a large clock top left, with the weather line, the calendar card and the year card stacked beneath it over the Northern Lights](docs/screenshots/features/screensaver.jpg)

Also: weather under the clock, light mode, and a PlayStation, Xbox or Switch tile when one is on HDMI. Weather and the continue-watching picture use the internet, so the assistant asks before turning them on. Details, keys and undo steps: `docs/launcher.md`.


Also: weather under the clock, light mode, and a PlayStation, Xbox or Switch tile when one is on HDMI. Details, keys and undo steps: `docs/launcher.md`.

---

## How It Works

Everything runs from your computer over ADB. Nothing is installed on the TV except the launcher and the apps you choose.

```mermaid
flowchart LR
    A[Turn ADB on<br>on the TV] --> B[Assistant or<br>scripts connect]
    B --> C[Dry run:<br>show the plan]
    C --> D{You approve<br>each step}
    D --> E[Debloat:<br>disable packages]
    D --> F[Speed:<br>shorter animations]
    D --> G[Launcher:<br>install APK, set Home]
    D --> H[Theme and options:<br>push config.json]
    D --> I[IPTV and<br>SmartTube]
    E & F & G & H & I --> J[Turn ADB off]
```

1. **Debloat.** `lists/` holds one package name per line; `scripts/debloat.sh` validates each, skips `lists/keep.txt`, runs `pm disable-user --user 0` on the rest and records what it disabled.
2. **Launcher.** `launcher/build-and-install.sh <tv-ip>` builds the APK, installs it, starts it, then disables the stock launcher and sets this one as Home, and restores the stock launcher if Home does not land on it.
3. **Themes and options.** `scripts/apply-theme.sh` pushes one `config.json` that overrides the launcher's built-in settings, so a theme changes or undoes without a rebuild.
4. **Pause on Home.** When Home is pressed with audio active, the launcher sends a pause media key; remove the `pauseMedia()` calls in `MainActivity.java` to turn it off.
5. **The assistant.** `START_HERE.md` is the script an assistant follows: it asks about your TV, shows each step and its dry run, and waits for approval before it changes anything.

---

## Repository Layout

| Path | Purpose |
|---|---|
| `START_HERE.md`, `SKILL.md` | The procedure an assistant follows, and the skill entry for Claude Code and Codex |
| `scripts/` | `connect`, `debloat`, `speed`, `apply-theme`, `revert` and the launcher-option tools, in shell and Python over ADB |
| `lists/` | Packages to disable (`debloat-google.txt`, `debloat-firmware.txt`) and the protected list (`keep.txt`) |
| `launcher/` | The launcher app (`com.gathin.tvhome`) and `build-and-install.sh` |
| `themes/`, `catalog/` | Theme presets and the app catalog offered for your dock |
| `iptv/` | The OwnTV playlist builder for the iptv-org index |
| `docs/` | Setup, safety, launcher options, troubleshooting |
| `tests/` | Unit tests that use a fake `adb`, so no TV is needed |

---

## Verification and Status

```bash
python3 -m unittest discover -s tests
```

- Tested: the scripts' dry-run and undo paths (fake ADB), the theme checker, the playlist verifier (a local test server), and the assistant entry points.
- Not tested here: a real TV run of these scripts. The launcher builds and runs on its author's TV; build it yourself first (`docs/launcher.md`). Test on your own TV first.
- The tests check the keep list, the setup-wizard and HOME guards, dry-run behaviour, the undo record, the theme checker and the entry points.

---

## Credits

- **Clean Your TV** by Gathin (MIT): the launcher in `launcher/`, and the safety rules that came from its device notes. See `NOTICE`.
- **iptv-org** (Unlicense): the playlist index used at run time.
- **OwnTV**, **SmartTube**: players you install separately from their own projects.
- Image credits: Mountains, Ansel Adams (public domain); Northern Lights, Johannes Groll (CC0); Violet Aurora, Stephan Sprinz (CC BY 4.0); the rest are generated or custom artwork (`launcher/THEME-CREDITS.md`). More screenshots, and how to take your own: `docs/screenshots/README.md`.

## License

MIT. See `LICENSE` and `NOTICE`.
