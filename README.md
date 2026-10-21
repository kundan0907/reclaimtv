# tv-cleanup

Clean up an Android TV or Google TV: remove apps you do not use (reversibly), shorten animations, set up IPTV in OwnTV, install SmartTube, and apply a launcher theme you choose. It works as a skill for Claude Code and Codex, or with any AI assistant that can read this repo.

![A glass Android TV launcher in eight themes: Northern Lights, Violet Aurora, Synthwave, Sunset, Carbon, Rosso Corsa, Iron Man arc reactor and Daylight](docs/screenshots/hero-themes.jpg)

<p align="center"><img src="docs/screenshots/theme-cycle.gif" width="640" alt="The launcher cycling through its 16 themes"></p>

## Before and after

| Before: stock Google TV home | After: tv-cleanup launcher |
|---|---|
| ![Stock Google TV home: a featured title, a top picks row and every app in one long row](docs/screenshots/stock-home-before.png) | ![tv-cleanup home: a five-app dock, the clock, Inputs and Settings, on a dark city background](docs/screenshots/home-gotham-after.png) |

What changed on the TV:

- The launcher replaces Google TV's home, so the featured title and "Top free picks" rows are not shown. You see the apps you chose.
- The dock holds the apps you pick, in the order you set. A theme sets which apps and how many.
- Google TV's extra apps and TV-maker promotions are disabled, not uninstalled (`lists/`). The Play Store and Downloader are never touched.
- Animations are shorter (`scripts/speed.sh`). Every change has an undo: `scripts/revert.sh`.

## The launcher

A glass launcher with 16 themes, an Apple-style magnifying dock and optional widgets. Everything is off until you choose it. You build and install the APK once (`docs/launcher.md`).

### Glass dock

The dock magnifies the app under focus, and a soft light in the theme's colour flows along it.

![The home screen: a glass dock with five apps, a row of apps, and a widget row with calendar, year, weather and a continue-watching tile](docs/screenshots/features/dock.jpg)

### Widgets that stay out of the way

Press Up and the dock slides down to a thin lip. The calendar and the year card stay on screen, top right, stacked under the three buttons.

| Dock tucked away | Widget row |
|---|---|
| ![Dock hidden: clock, weather, calendar and year card on the wallpaper](docs/screenshots/features/dock-hidden.jpg) | ![Widget row with calendar, year, weather and a continue-watching tile that shows the video you left](docs/screenshots/features/widget-row.jpg) |

The continue-watching tile shows the real thumbnail and title of the last video (Big Buck Bunny by the Blender Foundation, CC BY 3.0, in these pictures).

### Calendar and year

Open the calendar with OK. It shows the week or the whole month, and remembers your choice. The year card counts the days left.

| Weekly | Monthly | Days left in the year |
|---|---|---|
| ![Weekly calendar, October 18 to 24, today in green](docs/screenshots/features/calendar-weekly.jpg) | ![Monthly calendar for October 2026](docs/screenshots/features/calendar-monthly.jpg) | ![Year card: 71 days left, a dot for every day of 2026](docs/screenshots/features/year.jpg) |

### Settings on the TV

Everything is chosen on the TV with the remote: theme, theme rotation, widget size, glass or original icons, wallpaper.

| Menu | Theme picker | Launcher settings |
|---|---|---|
| ![The gear menu](docs/screenshots/features/menu.jpg) | ![Theme picker](docs/screenshots/features/theme-picker.jpg) | ![Launcher settings](docs/screenshots/features/launcher-settings.jpg) |

Also: weather under the clock, screensaver widgets, light mode, and a PlayStation, Xbox or Switch tile when one is on HDMI. Weather and the continue-watching picture use the internet, so the assistant asks before turning them on. Details, keys and undo steps: `docs/launcher.md`.

## Themes

Sixteen themes ship in the APK, each with its own accent colour that tints the dock light, the widgets and the focus glow. Pick one in gear > Theme, or let them rotate.

| | | |
|---|---|---|
| ![Northern Lights](docs/screenshots/themes/northern.jpg) | ![Violet Aurora](docs/screenshots/themes/violet.jpg) | ![Synthwave](docs/screenshots/themes/synthwave.jpg) |
| **Northern Lights** `#52E08A` | **Violet Aurora** `#C65BFF` | **Synthwave** `#FF4FD8` |
| ![Sunset](docs/screenshots/themes/sunset.jpg) | ![Aurora](docs/screenshots/themes/aurora.jpg) | ![Carbon](docs/screenshots/themes/carbon.jpg) |
| **Sunset** `#FF8A5B` | **Aurora** `#3FD0C0` | **Carbon** `#4DA3FF` |
| ![Gotham](docs/screenshots/themes/gotham.jpg) | ![Iron Man · Arc reactor](docs/screenshots/themes/ironman-arc.jpg) | ![Black Panther · Vibranium](docs/screenshots/themes/panther-vibranium.jpg) |
| **Gotham** `#A9B4C8` | **Iron Man · Arc reactor** `#35D6F5` | **Black Panther · Vibranium** `#8A5CF5` |
| ![RCB](docs/screenshots/themes/rcb.jpg) | ![Rosso Corsa](docs/screenshots/themes/rosso.jpg) | ![Mountains](docs/screenshots/themes/mountains.jpg) |
| **RCB** `#E0252B` | **Rosso Corsa** `#E03A3A` | **Mountains** `#8D99AE` |
| ![Daylight](docs/screenshots/themes/daylight.jpg) | ![Aurora · Light](docs/screenshots/themes/aurora-light.jpg) | ![Rosso Corsa · Light](docs/screenshots/themes/rosso-light.jpg) |
| **Daylight** `#5B8DEF` | **Aurora · Light** `#1FA896` | **Rosso Corsa · Light** `#C9302C` |
| ![Mountains · Light](docs/screenshots/themes/mountains-light.jpg) |   |   |
| **Mountains · Light** `#6B7A90` |   |   |

Five colour-only presets (`dark`, `batman`, `tesla`, `aurora`, `compact`) also ship in `themes/` and need no wallpaper. The Gotham picture in "Before and after" is from the author's TV. Details: `themes/README.md`.

Image credits: Mountains, Ansel Adams (public domain); Northern Lights, Johannes Groll (CC0); Violet Aurora, Stephan Sprinz (CC BY 4.0); the rest are generated or custom artwork (`launcher/THEME-CREDITS.md`). More screenshots, and how to take your own: `docs/screenshots/README.md`.

## Set up with an AI

Paste this into any AI assistant:

```text
Set up my Android TV with https://github.com/kundan0907/tv-cleanup — start with START_HERE.md and ask before each change.
```

The assistant asks about your TV, network, theme, apps and IPTV, then shows each step before it runs. A cloud chat can plan and guide, but it cannot reach a TV on your home network. For the ADB steps, use Claude Code or the Codex CLI on the same Wi-Fi.

## What you get

| Step | What it does | Undo |
|---|---|---|
| Debloat | Disables Google and TV-maker apps you do not use (`lists/`) | `scripts/revert.sh` |
| Speed | Halves animation scales | `scripts/revert.sh` |
| Launcher and theme | Dark, Batman, Tesla, Aurora or Compact (`themes/`), plus launcher options (`scripts/launcher_options.py`) | `scripts/apply-theme.sh <tv> --reset` |
| IPTV | Public channels from iptv-org, checked and filtered for OwnTV (`iptv/`) | Remove the playlist in OwnTV |
| SmartTube | Installed from its official release (`docs/smarttube.md`) | Uninstall it |
| Free movies | Legal free sources (`docs/free-movies.md`) | Not applicable |

Nothing is uninstalled. The Play Store and Downloader are never touched (`lists/keep.txt`). See `docs/safety.md`.

## Quick start

1. Paste the prompt from "Set up with an AI" into your assistant, or point Claude Code or Codex at this repo.
2. Turn on ADB on the TV (`docs/setup.md`) when the assistant asks.
3. Answer the questions. Each step is shown before it runs.

Optional: install it as a skill (see `SKILL.md`).

Run the scripts by hand if you prefer:

```bash
scripts/connect.sh 192.0.2.10
scripts/debloat.sh 192.0.2.10 light --dry-run
scripts/debloat.sh 192.0.2.10 light
scripts/apply-theme.sh 192.0.2.10 themes/dark
```

## Tests

```bash
python3 -m unittest discover -s tests
```

The tests use a fake `adb`, so they do not need a TV. They check the keep list, the setup-wizard and HOME guards, dry-run behaviour, the undo record, the theme checker and the AI entry points.

## Status

- Tested: the scripts' dry-run and undo paths (fake ADB), the theme checker, the playlist verifier (a local test server), and the AI entry points.
- Not tested here: a real TV run of these scripts. The launcher builds and runs on its author's TV; build it yourself first (`docs/launcher.md`). Test on your own TV first.

## Credits

- **Clean Your TV** by Gathin (MIT): the launcher in `launcher/`, and the safety rules that came from its device notes. See `NOTICE`.
- **iptv-org** (Unlicense): the playlist index used at run time.
- **OwnTV**, **SmartTube**: players you install separately from their own projects.

## License

MIT. See `LICENSE` and `NOTICE`.
